package com.jiangyifen.ec2.service.mgr.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import jxl.read.biff.BiffException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Transactional;

import au.com.bytecode.opencsv.CSVReader;

import com.jiangyifen.ec2.eao.AddressEao;
import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.eao.CompanyEao;
import com.jiangyifen.ec2.eao.CustomerResourceBatchEao;
import com.jiangyifen.ec2.eao.CustomerResourceDescriptionEao;
import com.jiangyifen.ec2.eao.CustomerResourceEao;
import com.jiangyifen.ec2.eao.MarketingProjectTaskEao;
import com.jiangyifen.ec2.eao.TableKeywordEao;
import com.jiangyifen.ec2.eao.TelephoneEao;
import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.TableKeywordDefault;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.entity.enumtype.CustomerQuestionnaireFinishStatus;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectTaskType;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectType;
import com.jiangyifen.ec2.service.mgr.ImportResourceService;
import com.vaadin.ui.ProgressIndicator;

/**
 * 
 * 调用导数据时传入的参数批次不能为null，名称不能为“”， 批次的创建者不能为空，域不能为空
 * 
 * @author chb
 * 
 */
public class ImportResourceServiceImpl2 implements ImportResourceService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private CustomerResourceBatchEao customerResourceBatchEao;
	private CustomerResourceEao customerResourceEao;
	private CustomerResourceDescriptionEao customerResourceDescriptionEao;
	private MarketingProjectTaskEao marketingProjectTaskEao;
	private TableKeywordEao tableKeywordEao;
	private TelephoneEao telephoneEao;
	private CompanyEao companyEao;
	private CommonEao commonEao;
	private AddressEao addressEao;

	/**
	 * 其他
	 */
	public static String HAS_CUSTOMER_IGNORE = "has_customer_ignore";// 是客户忽略导入几条
	public static String HAS_RESOURCE_UPDATE = "has_resource_update";// 已存在资源更新几条
	public static String IMPORT_SUCCESS = "import_success";			// 导入成功几条
	public static String INVALID_NUMBER = "invalid_number";			// 无效几条
	public static String ELAPSED_TIME = "elapsed_time";				// 总耗时

	public ImportResourceServiceImpl2() {
	}

	/**
	 * chb 导数据的方法
	 * 
	 * @param file
	 * @param batch
	 * @return 返回值为导入的条数，如果返回null表示导入失败
	 * @throws IOException
	 * @throws BiffException
	 */
	// TODO 
	// 这里使用 synchronized : 这个对于自建客户[单个域]来说,是没有问题的，但对于托管服务器而言，如果多个租户同时导数据就会导致其他租户等待时间过长的情况
	@Override
	public synchronized Map<String, Long>  importData(File file, CustomerResourceBatch batch,User user,
			Boolean isAppend,ProgressIndicator pi,Boolean quChong) {
		// 重建map集合用于计数
		Map<String, Long> resultMap = new HashMap<String, Long>();
		resultMap.put(HAS_CUSTOMER_IGNORE, 0L);
		resultMap.put(HAS_RESOURCE_UPDATE, 0L);
		resultMap.put(IMPORT_SUCCESS, 0L);
		resultMap.put(INVALID_NUMBER, 0L);
		resultMap.put(ELAPSED_TIME, 0L);
		
		// 向批次表中添加一条新的批次记录
		try {
			Long startTime=System.currentTimeMillis();
			if(batch!=null){
				batch = ((ImportResourceService) AopContext.currentProxy())
						.validatePersistNewBatch(batch); // 验证批次是否符合条件，得到一个持久化后的批次
			}
			Long endTime=System.currentTimeMillis();
			logger.info("验证批次耗时:"+(endTime-startTime)/1000+"秒");
			// 使持久化后的批次可以被全局调用

			if(file.getName().endsWith("csv")){
				// 将文件文件解析，并且存入数据库
				this.parseCsvFileAndPersistToResource(file.getAbsolutePath(), isAppend, batch,user,pi,resultMap,quChong);
			}else{
				// 将文件文件解析，并且存入数据库
				this.parseFileAndPersistToResource(file.getAbsolutePath(), isAppend, batch,user,pi,resultMap,quChong);
			}
			Long parseTime = System.currentTimeMillis();
			Long elapsedTime = (parseTime-startTime)/1000;
			resultMap.put(ELAPSED_TIME, elapsedTime);
			// 如果导入数据不成功，阻止事务运行！
		} catch (Exception e) {
			// 如果导入中途失败则查看是有导入成功的数据，如果没有则删除此批次
			if ((!isAppend)&&resultMap.get(IMPORT_SUCCESS) < 1) {
				((ImportResourceService) AopContext.currentProxy()).deleteBatch(batch);
			}
			e.printStackTrace();
			// 导数据可能预料到的异常已经全部被封装，如果不能预料的异常出现，则抛出异常
			throw new RuntimeException(e.getMessage());
		}
		return resultMap;
	}

	/**
	 * 如果导数据失败，删除批次 放在这里还public还transaction有点不好
	 */
	public void deleteBatch(CustomerResourceBatch batch) {
		// 应该是不为null的
		if (batch == null || batch.getId() == null)
			return;
		// 删除批次 ,此时批次一定还没有与项目进行关联
		String nativeSql = "delete from ec2_customer_resource_ec2_customer_resource_batch where customerresourcebatches_id="
				+ batch.getId();
		String nativeSql1 = "delete from ec2_customer_resource_batch where id="
				+ batch.getId();
		customerResourceBatchEao.getEntityManager()
				.createNativeQuery(nativeSql).executeUpdate();
		customerResourceBatchEao.getEntityManager()
				.createNativeQuery(nativeSql1).executeUpdate();

	}

	/**
	 * 验证批次是否合法,如果没有日期,添加现在的时间为日期,并将批次存入数据库
	 * 
	 * @param batch
	 * @return 返回一个持久化后的批次
	 */
	public CustomerResourceBatch validatePersistNewBatch(
			CustomerResourceBatch batch) {
		// 对于批次进行验证，如果批次不符合条件抛出异常信息
		if (batch == null || batch.getBatchName() == null
				|| batch.getBatchName().trim().equals("")) {
			throw new RuntimeException("批次名称不能为空！");
		}
		if (batch.getUser() == null || batch.getDomain() == null) {
			throw new RuntimeException("批次必须有创建者Owner和Domain！");
		}
		if (batch.getCreateDate() == null) {
			batch.setCreateDate(new Date());
		}
		// 向批次表持久化一个新的批次
		return (CustomerResourceBatch) customerResourceBatchEao.update(batch);
	}

	/**
	 * 判断excel 2007 并获取
	 * @param fullpath
	 * @return
	 * @throws Exception
	 */
	 private Workbook getWorkbook(String fullpath) throws Exception{
			Boolean is2007=false;
		 	if (-1 < fullpath.indexOf(".xlsx")) {
		 		is2007=true;
			}else{
				is2007=false;
			}
	        return is2007?new XSSFWorkbook(new FileInputStream(fullpath)):new HSSFWorkbook(new FileInputStream(fullpath));  
	 }  
	 
	 /**
	  * 解析CSV并存储到数据库
	  * 
	  * @return
	  * @throws IOException
	  */
	 private void parseCsvFileAndPersistToResource(String fullpath, Boolean isAppend,
			 CustomerResourceBatch batch,User user,ProgressIndicator pi, Map<String, Long> resultMap,Boolean quChong) throws IOException {

         //统计CSV文件总行数
         Long totalRow = 0L;
		 
		 InputStream input = new FileInputStream(fullpath);
		 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input,"GBK"));
		 String value = bufferedReader.readLine();
		 while(value !=null){
			 totalRow++;
			 value = bufferedReader.readLine();
		 }
		 bufferedReader.close();

		 
		FileReader fileReader = new FileReader(fullpath);
		CSVReader reader = new CSVReader(fileReader);
        String [] nextLine = reader.readNext();
        if(nextLine==null){
        	reader.close();
        	throw new RuntimeException("CSV 文件不能为空");
        }
        
        //文件头处理
        Long startTime=System.currentTimeMillis();
		 HashMap<Integer, String> headers = parseCsvHeader(nextLine, user.getDomain());
		 Long endTime = System.currentTimeMillis();
		 logger.info("解析CSV文件头耗时:"+(endTime-startTime)/1000+"秒");
        
		 Long startTime1=System.currentTimeMillis();
		 
		 Long processRow = 1L;
		 while ((nextLine = reader.readNext()) != null) {
			 parseOneCsvRow(nextLine, headers, isAppend, batch,user, pi, processRow, totalRow, resultMap,quChong);
		 }
		 reader.close();
		 
		 Long endTime1=System.currentTimeMillis();
		 logger.info("解析CSV文件耗时:"+(endTime1-startTime1)/1000+"秒");
	 }
	 
	/**
	 * 将文件分段解析并存储到数据库
	 * 
	 * @return
	 * @throws IOException
	 * @throws BiffException
	 */
	private void parseFileAndPersistToResource(String fullpath, Boolean isAppend,
			CustomerResourceBatch batch,User user,ProgressIndicator pi, Map<String, Long> resultMap,Boolean quChong) throws BiffException, IOException {
		Workbook book = null;
		try {
			book = getWorkbook(fullpath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("不能识别的文件格式，请选择Excel文件作为上传对象");
		}
		
		int sheetnum=book.getNumberOfSheets();
		List<Sheet> sheetList=new ArrayList<Sheet>();  
		for(int i=0;i<sheetnum;i++){
			sheetList.add(book.getSheetAt(0));
		}
		
		// 获的多页工作表对象
		// 统计整个Excel 的总行数，用于更新进度条的显示进度
		Long totalRow = 0L;
		for(int i=0;i<sheetList.size();i++) {
			int sheetrowcount=sheetList.get(i).getPhysicalNumberOfRows();
			logger.info("sheet [" +i+ "]row 行数--》 " + sheetrowcount);
			totalRow += sheetrowcount -1;
		}
		
		Long completeRows = 0L;
		for(int i=0;i<sheetList.size();i++) {
			Sheet sheet = sheetList.get(i);
			// 如果一页中没有数据，则继续到下一页
			if (sheet.getPhysicalNumberOfRows() == 0)
				continue;
			// 从Sheet中解析出列的对应信息
			Long startTime=System.currentTimeMillis();
			HashMap<Integer, String> headers = parseExcelHeader(sheet, user.getDomain());
			Long endTime = System.currentTimeMillis();
			logger.info("解析文件头耗时:"+(endTime-startTime)/1000+"秒");
			
			Long startTime1=System.currentTimeMillis();
			
			parseOneSheet(sheet, headers, isAppend, batch,user, pi, completeRows, totalRow, resultMap,quChong);
			
			completeRows += (long) sheet.getPhysicalNumberOfRows();
			Long endTime1=System.currentTimeMillis();
			logger.info("解析一页文件耗时:"+(endTime1-startTime1)/1000+"秒");
		}
	}
	
	/**
	 * 解析一条的资源
	 * 
	 * @param sheet
	 * @param headers
	 * @return
	 * @return
	 */
	private Map<String, Long> parseOneCsvRow(String [] nextLine,
			HashMap<Integer, String> headers, Boolean isAppend,
			CustomerResourceBatch batch,User user,ProgressIndicator pi, 
			Long completeRows, Long totalRow, Map<String, Long> resultMap,Boolean quChong) {
		try {
			List<CustomerResource> customerResources = ((ImportResourceService) AopContext
					.currentProxy()).persistOneCsvRowToCustomerResourceList(nextLine, headers, completeRows.intValue(), isAppend, batch, user, resultMap,quChong);// 一行一个事物进行存储
			
			//由于涉及到事务问题，所以在存储了Resource之后才存储Task，可能与原生Sql先执行有关系
			// jrh 如果是追加数据并且批次不为null
			if(isAppend && batch!=null) {
				for(CustomerResource customerResource : customerResources) {
					((ImportResourceService) AopContext.currentProxy()).persistToTask(batch,customerResource, user.getDomain());
				}
			}
			
			pi.setValue((completeRows+completeRows)/(float)totalRow);
		} catch (Exception e) {
			logger.info("问题行： 行号-->"+completeRows+"  内容-->"+nextLine);
			// 认为加的Try catch 以使导数据不至中断，一行出现的异常
			e.printStackTrace();
		}
		return resultMap;
	}

	/**
	 * 解析一页的资源
	 * 
	 * @param sheet
	 * @param headers
	 * @return
	 * @return
	 */
	private Map<String, Long> parseOneSheet(Sheet sheet,
			HashMap<Integer, String> headers, Boolean isAppend,
			CustomerResourceBatch batch,User user,ProgressIndicator pi, 
			Long completeRows, Long totalRow, Map<String, Long> resultMap,Boolean quChong) {
		// 从第二行开始解析，因为第一行为表格头部
		int row = sheet.getPhysicalNumberOfRows();
		for (int r = 1; r < row; r++) {
			try {
				List<CustomerResource> customerResources = ((ImportResourceService) AopContext.currentProxy()).persistOneRow(sheet, headers, r, isAppend, batch, user, resultMap,quChong);// 一行一个事物进行存储
				//由于涉及到事务问题，所以在存储了Resource之后才存储Task，可能与原生Sql先执行有关系
				// jrh 如果是追加数据并且批次不为null
				if(isAppend && batch!=null) {
					for(CustomerResource customerResource : customerResources) {
						((ImportResourceService) AopContext.currentProxy()).persistToTask(batch,customerResource, user.getDomain());
					}
				}

				pi.setValue((completeRows+r)/(float)totalRow);
			} catch (Exception e) {
				try {
					Row tmprow= sheet.getRow(r);
					StringBuilder stringBuilder=new StringBuilder();
					for (int j = tmprow.getFirstCellNum(); j < tmprow.getPhysicalNumberOfCells(); j++) {  
					    // 通过 row.getCell(j).toString() 获取单元格内容，
						 Cell cell = tmprow.getCell(j);
						 if(cell!=null){
							 stringBuilder.append(" "+cell.getStringCellValue());
						 }
					}  
					
					logger.info("问题行： 行号-->"+r+"  内容-->"+stringBuilder.toString());
					// 认为加的Try catch 以使导数据不至中断，一行出现的异常
					e.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
					// do nothing but continue;
				}
			}
		}
		return resultMap;
	}
	/**
	 * 按照批次和项目的对应关系对资源生成批次对应的项目个数条Task，是不是追加在调用它的方法中进行验证,在接口中支持事物
	 * @param batch
	 * @param customerResource
	 */
	@SuppressWarnings("unchecked")
	public void persistToTask(CustomerResourceBatch batch,CustomerResource customerResource, Domain domain) {
		if(customerResource==null) return;
		// 如果是成功导入或者更新Resource数据，并且是追加资源则在存储Resource之后再将Resource存储到相应的Task中（每个项目都有一条相应的Task）
		// 如果是追加的资源，并且通过IMPORT_SUCCESS知道是新添加的资源，从批次项目中间表中查询出此批次对应的项目则将为资源创建Task
		// 通过批次取得对应n个项目的Id值
		String nativeSql = "select marketingproject_id from ec2_markering_project_ec2_customer_resource_batch where batches_id="
				+ batch.getId();
		List<Long> projectIdList = ((List<Long>) commonEao.excuteNativeSql(
				nativeSql, ExecuteType.RESULT_LIST));
		//此处多加了一个Set目的是节省查询的资源，不使用distinct
		Set<Long> projectIds=new HashSet<Long>(projectIdList);
		
		Boolean isValidTask=false;
		if(batch.getBatchStatus()==BatchStatus.USEABLE){
			isValidTask=true;
		}
		
		for (Long projectId : projectIds) {
			MarketingProject project = (MarketingProject) commonEao.excuteSql("select mp from MarketingProject as mp where mp.id = "+projectId, ExecuteType.SINGLE_RESULT);
			if(project != null) {
				marketingProjectTaskEao.checkThenAddTask(customerResource, batch.getId(),isValidTask, project, domain.getId());
			}
		}
	}
	
	/**
	 * 将一行保存为一条资源，并将保存结果存入resultMap中 这一行含有事物控制
	 */
	@Override
	public List<CustomerResource> persistOneCsvRowToCustomerResourceList(String [] nextLine,
			HashMap<Integer, String> headers, int r, Boolean isAppend,
			CustomerResourceBatch batch,User user, Map<String, Long> resultMap,Boolean quChong) {
		Boolean isSuccessOrUpdate = false;
		// 按照行号和标题头将Excel中的一行接卸为一个CustomerResource
		List<CustomerResource> customerResources = null;
		// 其实只有一个结果用来充当函数的返回值
		List<String> result = new ArrayList<String>();
		result.add("initial");
		try {
			customerResources = parseOneRecordToCustomerResource(nextLine, headers, r, result,user);
		} catch (Exception e) {// 此处的异常为Exception异常，能捕捉所有异常，不至于程序停止
			// 将出现异常的数量视为无效号码
			resultMap.put(INVALID_NUMBER, resultMap.get(INVALID_NUMBER) + 1);
			logger.info("chb_import_data_exception");
		}
		
		// jrh 为资源添加一个新批次的关联(无论是客户、还是已存在资源、或者是新增资源，只要是excel 中出现过的电话号码对应的对象，都应该与当前批次关联上
		for(CustomerResource customerResource : customerResources) {
			//如果批次不为null
			if(batch!=null){
				Set<CustomerResourceBatch> batchSet = customerResource.getCustomerResourceBatches();// .add(batch)
				if (batchSet == null) {
					batchSet = new HashSet<CustomerResourceBatch>();
				}
				batchSet.add(batch);
				customerResource.setCustomerResourceBatches(batchSet);
			}
			// 存储一条客户信息
			customerResource = (CustomerResource) customerResourceEao.update(customerResource);
		}
		
		// 对CustomerResource进行计数操作
		if (result.get(0).equals(HAS_CUSTOMER_IGNORE)) {// 已经存在的客户资源，
			resultMap.put(HAS_CUSTOMER_IGNORE, resultMap.get(HAS_CUSTOMER_IGNORE) + 1);
			isSuccessOrUpdate = true;
//			return new ArrayList<CustomerResource>(); // 不存储
		} else if (result.get(0).equals(IMPORT_SUCCESS)) {
			resultMap.put(IMPORT_SUCCESS, resultMap.get(IMPORT_SUCCESS) + 1);
			isSuccessOrUpdate = true;
		} else if (result.get(0).equals(INVALID_NUMBER)) {
			// 没有有效号码
			resultMap.put(INVALID_NUMBER, resultMap.get(INVALID_NUMBER) + 1);
			return new ArrayList<CustomerResource>(); // 不存储
		} else if (result.get(0).equals(HAS_RESOURCE_UPDATE)) {
			resultMap.put(HAS_RESOURCE_UPDATE, resultMap.get(HAS_RESOURCE_UPDATE) + 1);
			isSuccessOrUpdate = true;
		} else if (result.get(0).equals("initial")) {
			throw new RuntimeException("不应该没有结果类型");
		}
		
		if (isSuccessOrUpdate) {
			return customerResources;
		} else {
			return null;
		}
	}

	/**
	 * 将一行保存为一条资源，并将保存结果存入resultMap中 这一行含有事物控制
	 */
	@Override
	public List<CustomerResource> persistOneRow(Sheet sheet,
			HashMap<Integer, String> headers, int r, Boolean isAppend,
			CustomerResourceBatch batch,User user, Map<String, Long> resultMap,Boolean quChong) {
		Boolean isSuccessOrUpdate = false;
		// 按照行号和标题头将Excel中的一行接卸为一个CustomerResource
		List<CustomerResource> customerResources = null;
		// 其实只有一个结果用来充当函数的返回值
		List<String> result = new ArrayList<String>();
		result.add("initial");
		try {
			customerResources = parseOneRecordToCustomerResource(sheet, headers, r, result,user,quChong);
		} catch (Exception e) {// 此处的异常为Exception异常，能捕捉所有异常，不至于程序停止
			// 将出现异常的数量视为无效号码
			resultMap.put(INVALID_NUMBER, resultMap.get(INVALID_NUMBER) + 1);
			logger.info("chb_import_data_exception");
		}

		// jrh 为资源添加一个新批次的关联(无论是客户、还是已存在资源、或者是新增资源，只要是excel 中出现过的电话号码对应的对象，都应该与当前批次关联上
		for(CustomerResource customerResource : customerResources) {
			//如果批次不为null
			if(batch!=null){
				Set<CustomerResourceBatch> batchSet = customerResource.getCustomerResourceBatches();// .add(batch)
				if (batchSet == null) {
					batchSet = new HashSet<CustomerResourceBatch>();
				}
				batchSet.add(batch);
				customerResource.setCustomerResourceBatches(batchSet);
			}
			// 存储一条客户信息
			customerResource = (CustomerResource) customerResourceEao.update(customerResource);
		}
		
		// 对CustomerResource进行计数操作
		if (result.get(0).equals(HAS_CUSTOMER_IGNORE)) {// 已经存在的客户资源，
			resultMap.put(HAS_CUSTOMER_IGNORE, resultMap.get(HAS_CUSTOMER_IGNORE) + 1);
			isSuccessOrUpdate = true;
//			return new ArrayList<CustomerResource>(); // 不存储
		} else if (result.get(0).equals(IMPORT_SUCCESS)) {
			resultMap.put(IMPORT_SUCCESS, resultMap.get(IMPORT_SUCCESS) + 1);
			isSuccessOrUpdate = true;
		} else if (result.get(0).equals(INVALID_NUMBER)) {
			// 没有有效号码
			resultMap.put(INVALID_NUMBER, resultMap.get(INVALID_NUMBER) + 1);
			return new ArrayList<CustomerResource>(); // 不存储
		} else if (result.get(0).equals(HAS_RESOURCE_UPDATE)) {
			resultMap.put(HAS_RESOURCE_UPDATE, resultMap.get(HAS_RESOURCE_UPDATE) + 1);
			isSuccessOrUpdate = true;
		} else if (result.get(0).equals("initial")) {
			throw new RuntimeException("不应该没有结果类型");
		}
		
		if (isSuccessOrUpdate) {
			return customerResources;
		} else {
			return null;
		}
	}

	/**
     * 获取Excel Cell内容
     * @return
     */
    public static String getContent(Cell cell){
	    if(cell==null){
	    	return "";
	    }
	    
	    String content="";
	    	
        if(cell.getCellType()==Cell.CELL_TYPE_BLANK){
        	content="";
        }else if(cell.getCellType()==Cell.CELL_TYPE_BOOLEAN){
        	content=""+cell.getBooleanCellValue();
        }else if(cell.getCellType()==Cell.CELL_TYPE_ERROR){
        	content=""+cell.getErrorCellValue();
        }else if(cell.getCellType()==Cell.CELL_TYPE_FORMULA){
        	content="";
        }else if(cell.getCellType()==Cell.CELL_TYPE_NUMERIC){
        	DecimalFormat df = new DecimalFormat("#");
        	content=df.format(cell.getNumericCellValue());
        }else if(cell.getCellType()==Cell.CELL_TYPE_STRING){
        	content=""+cell.getStringCellValue();
        }else{
        	content="";
        }
        
        return content.trim();
	}
	
    /**
     * 解析Csv一行为一个CustomerResource
     * 
     * @return 
     *         如果返回null表示Resource为客户，跳过，返回CustomerResource如果带Id，表示对客户资源进行更新，不带Id表示导入成功
     */
    private List<CustomerResource> parseOneRecordToCustomerResource(String[] nextLine,  
    		HashMap<Integer, String> headers, int rowNum, List<String> result,User user) {
    	
    	Map<Long, CustomerResource> resourceMap = new HashMap<Long, CustomerResource>();
    	List<CustomerResource> resources = new ArrayList<CustomerResource>();
    	Domain domain = user.getDomain();
    	
    	// 所有表格头部的编号
    	Set<Integer> allHeaderSet = new HashSet<Integer>(headers.keySet()); // 重新包装，以防和headers中冲突
    	// 按照电话、姓名、性别、生日、公司、地址、描述信息的顺序进行处理
    	List<Integer> phoneList = new ArrayList<Integer>();
    	for (Integer headerIndex : allHeaderSet) {
    		if (headers.get(headerIndex).equals(TableKeywordDefault.PHONE.getName())) {
    			phoneList.add(headerIndex);
    		}
    	}
    	// 移除电话的标号
    	allHeaderSet.removeAll(phoneList);
    	/**
    	 * ==============================对电话号码进行处理==============================
    	 **/
    	// 不是空字符串也不是客户的电话号码
    	Set<String> useablePhoneList = new HashSet<String>();
    	// 存储通过电话号码选出来的不是客户的资源
    	CustomerResource customerResource = null;
    	// 记录是不是曾经查找到客户
    	Boolean isFindCustomerResource = false;
    	for (Integer phoneIndex : phoneList) {
    		String phoneNumber = nextLine[phoneIndex];
    		// 对电话号进行去前面0，和去非数字处理
    		phoneNumber = phoneNumberProcess(phoneNumber);
    		// 不是空字符串
    		if (phoneNumber == null || phoneNumber.equals("")) {
    			continue;
    		}
    		// 查询到返回resource，查询不到返回null
    		CustomerResource tempResource = customerResourceEao.getCustomerResourceByPhoneNumber(phoneNumber, domain.getId());
    		
    		// 没有出现在数据库中的电话号码
    		if (tempResource == null) {
    			useablePhoneList.add(phoneNumber);// 添加到集合
    		} else { // 电话号码出现在数据库中，已经被CustomerResource占有（此时还不知道是不是客户）
    			resourceMap.put(tempResource.getId(), tempResource);	// jrh 如果资源已经存在，则先存入Map
    			// 看占有电话号码的资源是不是客户
    			Boolean isCustomer = customerResourceEao.isCustomerById(tempResource.getId(), domain);
    			if(!isCustomer) {	// jrh 正常情况下只需要检查是否有客户经理就可以了，上面的查询语句可以省略
    				isCustomer = (tempResource.getAccountManager() != null);
    			}
    			// 是客户就不处理此条电话号码
    			if (isCustomer) {
    				isFindCustomerResource = true;// 标记这条资源中含有客户
    				continue;
    			} else {// 否则记录查找到的客户资源
    				customerResource = tempResource;
    			}
    		}
    	}
    	
    	if (isFindCustomerResource) {// 如果查找到客户
    		result.set(0, HAS_CUSTOMER_IGNORE);
    		resources.addAll(resourceMap.values());
    		return resources; // 标识是已经存在的客户资源
    	}
    	
    	// 此条记录中有没有存储过的电话号码，看是不是有非客户资源，如果有就将信息存储到非客户资源，如果没有就新建客户存储信息
    	if (useablePhoneList.size() > 0) {
    		if (customerResource != null) {// 是客户资源则更新客户资源信息
    			Set<Telephone> telephones = new HashSet<Telephone>();
    			for (String phoneNumber : useablePhoneList) {
    				Telephone telephone = new Telephone();
    				telephone.setNumber(phoneNumber);
    				telephone.setDomain(domain);
    				telephone.setCustomerResource(customerResource);
    				telephone = (Telephone) telephoneEao.update(telephone);
    				telephones.add(telephone);
    			}
    			result.set(0, HAS_RESOURCE_UPDATE);
    			customerResource.setTelephones(telephones);
    		} else {// 不是客户资源，则新建资源，并存储电话号码
    			// 对于下面的Else中代码应该没有影响
    			Set<Telephone> telephones = new HashSet<Telephone>();
    			customerResource = new CustomerResource();
    			customerResource.setDomain(domain);
    			customerResource.setImportDate(new Date());
    			customerResource = (CustomerResource) customerResourceEao.update(customerResource);// 先对资源进行存储
    			for (String phoneNumber : useablePhoneList) {
    				Telephone telephone = new Telephone();
    				telephone.setNumber(phoneNumber);
    				telephone.setDomain(domain);
    				telephone.setCustomerResource(customerResource);
    				telephone = (Telephone) telephoneEao.update(telephone); 
    				telephones.add(telephone);
    			}
    			result.set(0, IMPORT_SUCCESS);
    			customerResource.setTelephones(telephones);
    		}
    		// 此条记录中所有电话号码均被存储，或者是此条记录中没有任意一条有效电话号码，
    		// 如果所有电话号码均被存储，不再进行存储处理，但应该就这条资源是不是含有客户信息做记录
    		// 如果没有有效电话则通过返回Id值为负1的CustomerResource计数
    	} else {
    		if (customerResource == null) {// 说明不是客户并且不是已经存在的资源（没有有效电话号码）
    			result.set(0, INVALID_NUMBER);
    			return resources;
    		} else {// 说明是客户资源，但是没有电话号码需要更新
    			// 更新完数据才进行返回 return customerResource;
    			result.set(0, HAS_RESOURCE_UPDATE);
    		}
    	}
    	
    	/** =================电话号码处理完毕==================== **/
    	
    	/** =================处理基本字段的存储或则是更新==为节省代码此处外加公司字段================== **/
    	// 基本字段包含姓名、性别、生日
    	// 有且仅有一个姓名
    	Integer nameIndex = -1;
    	Integer sexIndex = -1;
    	Integer birthdayIndex = -1;
    	Integer companyIndex = -1;
    	for (Integer headerIndex : allHeaderSet) {
    		if (headers.get(headerIndex).equals(
    				TableKeywordDefault.NAME.getName())) {
    			nameIndex = headerIndex;
    			String name = nextLine[headerIndex];
    			if (!name.equals("")) {
    				customerResource.setName(name);
    			}
    		} else if (headers.get(headerIndex).equals(
    				TableKeywordDefault.SEX.getName())) {
    			sexIndex = headerIndex;
    			String sex = nextLine[headerIndex];
    			if (!sex.equals("")) {
    				customerResource.setSex(sex);
    			}
    		} else if (headers.get(headerIndex).equals(
    				TableKeywordDefault.BIRTHDAY.getName())) {
    			birthdayIndex = headerIndex;
    			String birthday = nextLine[headerIndex];
    			if (!birthday.equals("")) {
    				customerResource.setBirthdayStr(birthday);
    			}
    		} else if (headers.get(headerIndex).equals(
    				TableKeywordDefault.COMPANY.getName())) {
    			companyIndex = headerIndex;
    			String companyName = nextLine[headerIndex];
    			if (companyName.equals(""))
    				continue;
    			Company company = companyEao.getCompanyByName(companyName,
    					domain.getId());
    			if (company == null) {// 注公司并没有在此处考虑地址 & 电话 两个概念
    				company = new Company();
    				company.setName(companyName);
    				company.setDomain(domain);
    				company = (Company) companyEao.update(company);
    			}
    			// 由于此处还没有存储resource已经存储了company，所以应该不会出错
    			customerResource.setCompany(company);// 如果是非客户资源，公司也会被更新
    		}
    	}
    	// 所有上面的Integer都应该是存在的
    	allHeaderSet.remove(nameIndex);
    	allHeaderSet.remove(sexIndex);
    	allHeaderSet.remove(birthdayIndex);
    	allHeaderSet.remove(companyIndex);
    	/**
    	 * =================处理基本字段的存储或则是更新==为节省代码此处外加公司字段 处理完毕==================
    	 **/
    	
    	/** =================对地址字段处理================== **/
    	List<Integer> addressList = new ArrayList<Integer>();
    	for (Integer headerIndex : allHeaderSet) {
    		if (headers.get(headerIndex).equals(
    				TableKeywordDefault.ADDRESS.getName())) {
    			addressList.add(headerIndex);
    		}
    	}
    	allHeaderSet.removeAll(addressList);
    	// 对所有地址字段进行循环处理
    	for (Integer addressIndex : addressList) {
    		String addressStr = nextLine[addressIndex];
    		if (addressStr.equals("")) {
    			continue;
    		}
    		Address address = new Address();
    		address.setStreet(addressStr);// 目前将地址存储到street属性中
    		address.setName(customerResource.getName()); // Note:目前程序只处理地址和姓名两项内容
    		address.setCustomerResource(customerResource);
    		address = (Address) addressEao.update(address);
    		customerResource.getAddresses().add(address);
    		customerResource.setDefaultAddress(address);
    	}
    	/** =================对地址字段处理 处理完毕================== **/
    	
    	/** =================对描述信息进行处理================== **/
    	// 剩下的表格未处理列都为描述信息
    	for (Integer descIndex : allHeaderSet) {
    		String key = headers.get(descIndex);
    		String value = nextLine[descIndex];
    		
//			if (value.equals("")) {
//				continue;
//			} //value 为空也同样存储
    		
    		// 此处添加根据key，value和customerResource的Id值去查找描述信息
    		// 如果存在，不重新将描述信息插入到数据库，如果不存在则新建描述信息
    		// 此处不考虑因为事务的，同一条记录不同列描述信息重复的情况
    		/*Boolean isExistDescription = customerResourceDescriptionEao.isExistDescription(key, value, customerResource.getId(),domain.getId());
    		if (isExistDescription) {
    			continue;
    		} */
    		value=value==null?"":value;
    		CustomerResourceDescription desc = customerResourceDescriptionEao.getExistDescription(key, value, customerResource.getId(), domain.getId());
    		if(desc == null) {
    			desc = new CustomerResourceDescription();
    			desc.setCreateDate(new Date());
    			desc.setLastUpdateDate(new Date());
    		} else {
    			desc.setLastUpdateDate(new Date());
    		}
    		desc.setDomain(domain);
    		desc.setKey(key);
    		desc.setValue(value);
    		desc.setCustomerResource(customerResource);
    		customerResourceDescriptionEao.update(desc);
    	}
    	/** =================对描述信息进行处理 处理完毕================== **/
    	
    	customerResource.setOwner(user);
    	customerResource.setCount(0);
    	customerResource.setLastDialDate(null);
    	customerResource.setExpireDate(new Date(0));
    	
    	// jrh
    	resourceMap.remove(customerResource.getId());
    	resourceMap.put(customerResource.getId(), customerResource);
    	resources.addAll(resourceMap.values());
    	return resources; // 返回之后会有事物处理
    }
	/**
	 * 解析Excel表中的一个Sheet的一行为一个CustomerResource
	 * 
	 * @return 
	 *         如果返回null表示Resource为客户，跳过，返回CustomerResource如果带Id，表示对客户资源进行更新，不带Id表示导入成功
	 */
	private List<CustomerResource> parseOneRecordToCustomerResource(Sheet sheet,  
			HashMap<Integer, String> headers, int rowNum, List<String> result,User user,Boolean quChong) {
		
		Map<Long, CustomerResource> resourceMap = new HashMap<Long, CustomerResource>();
		List<CustomerResource> resources = new ArrayList<CustomerResource>();
		Domain domain = user.getDomain();
		
		// 所有表格头部的编号
		Set<Integer> allHeaderSet = new HashSet<Integer>(headers.keySet()); // 重新包装，以防和headers中冲突
		// 按照电话、姓名、性别、生日、公司、地址、描述信息的顺序进行处理
		List<Integer> phoneList = new ArrayList<Integer>();
		for (Integer headerIndex : allHeaderSet) {
			if (headers.get(headerIndex).equals(TableKeywordDefault.PHONE.getName())) {
				phoneList.add(headerIndex);
			}
		}
		// 移除电话的标号
		allHeaderSet.removeAll(phoneList);
		/**
		 * ==============================对电话号码进行处理==============================
		 **/
		// 不是空字符串也不是客户的电话号码
		Set<String> useablePhoneList = new HashSet<String>();
		// 存储通过电话号码选出来的不是客户的资源
		CustomerResource customerResource = null;
		// 记录是不是曾经查找到客户
		Boolean isFindCustomerResource = false;
		for (Integer phoneIndex : phoneList) {
			Cell phoneNumberCell = sheet.getRow(rowNum).getCell(phoneIndex);
			String phoneNumber = getContent(phoneNumberCell);
			// 对电话号进行去前面0，和去非数字处理
			phoneNumber = phoneNumberProcess(phoneNumber);
			// 不是空字符串
			if (phoneNumber == null || phoneNumber.equals("")) {
				continue;
			}
			// 查询到返回resource，查询不到返回null
			CustomerResource tempResource = customerResourceEao.getCustomerResourceByPhoneNumber(phoneNumber, domain.getId());
			//chenhb: 220140728 如果是去重，不对资源进行任何处理
			if(tempResource!=null && quChong){
				result.set(0, HAS_RESOURCE_UPDATE);
				return new ArrayList<CustomerResource>();
			}
			
			// 没有出现在数据库中的电话号码
			if (tempResource == null) {
				useablePhoneList.add(phoneNumber);// 添加到集合
			} else { // 电话号码出现在数据库中，已经被CustomerResource占有（此时还不知道是不是客户）
				resourceMap.put(tempResource.getId(), tempResource);	// jrh 如果资源已经存在，则先存入Map
				// 看占有电话号码的资源是不是客户
				Boolean isCustomer = customerResourceEao.isCustomerById(tempResource.getId(), domain);
				if(!isCustomer) {	// jrh 正常情况下只需要检查是否有客户经理就可以了，上面的查询语句可以省略
					isCustomer = (tempResource.getAccountManager() != null);
				}
				// 是客户就不处理此条电话号码
				if (isCustomer) {
 					isFindCustomerResource = true;// 标记这条资源中含有客户
					continue;
				} else {// 否则记录查找到的客户资源
					customerResource = tempResource;
				}
			}
		}

		if (isFindCustomerResource) {// 如果查找到客户
			result.set(0, HAS_CUSTOMER_IGNORE);
			resources.addAll(resourceMap.values());
			return resources; // 标识是已经存在的客户资源
		}

		// 此条记录中有没有存储过的电话号码，看是不是有非客户资源，如果有就将信息存储到非客户资源，如果没有就新建客户存储信息
		if (useablePhoneList.size() > 0) {
			if (customerResource != null) {// 是客户资源则更新客户资源信息
				
				Set<Telephone> telephones = new HashSet<Telephone>();
				for (String phoneNumber : useablePhoneList) {
					Telephone telephone = new Telephone();
					telephone.setNumber(phoneNumber);
					telephone.setDomain(domain);
					telephone.setCustomerResource(customerResource);
					telephone = (Telephone) telephoneEao.update(telephone);
					telephones.add(telephone);
				}
				result.set(0, HAS_RESOURCE_UPDATE);
				customerResource.setTelephones(telephones);
				
			} else {// 不是客户资源，则新建资源，并存储电话号码
					// 对于下面的Else中代码应该没有影响
				Set<Telephone> telephones = new HashSet<Telephone>();
				customerResource = new CustomerResource();
				customerResource.setDomain(domain);
				customerResource.setImportDate(new Date());
				customerResource = (CustomerResource) customerResourceEao.update(customerResource);// 先对资源进行存储
				for (String phoneNumber : useablePhoneList) {
					Telephone telephone = new Telephone();
					telephone.setNumber(phoneNumber);
					telephone.setDomain(domain);
					telephone.setCustomerResource(customerResource);
					telephone = (Telephone) telephoneEao.update(telephone); 
					telephones.add(telephone);
				}
				result.set(0, IMPORT_SUCCESS);
				customerResource.setTelephones(telephones);
			}
			// 此条记录中所有电话号码均被存储，或者是此条记录中没有任意一条有效电话号码，
			// 如果所有电话号码均被存储，不再进行存储处理，但应该就这条资源是不是含有客户信息做记录
			// 如果没有有效电话则通过返回Id值为负1的CustomerResource计数
		} else {
			if (customerResource == null) {// 说明不是客户并且不是已经存在的资源（没有有效电话号码）
				result.set(0, INVALID_NUMBER);
				return resources;
			} else {// 说明是客户资源，但是没有电话号码需要更新
				// 更新完数据才进行返回 return customerResource;
				result.set(0, HAS_RESOURCE_UPDATE);
			}
		}
		
		/** =================电话号码处理完毕==================== **/

		/** =================处理基本字段的存储或则是更新==为节省代码此处外加公司字段================== **/
		// 基本字段包含姓名、性别、生日
		// 有且仅有一个姓名
		Integer nameIndex = -1;
		Integer sexIndex = -1;
		Integer birthdayIndex = -1;
		Integer companyIndex = -1;
		for (Integer headerIndex : allHeaderSet) {
			if (headers.get(headerIndex).equals(
					TableKeywordDefault.NAME.getName())) {
				nameIndex = headerIndex;
				String name = getContent(sheet.getRow(rowNum).getCell(headerIndex));
				if (!name.equals("")) {
					customerResource.setName(name);
				}
			} else if (headers.get(headerIndex).equals(
					TableKeywordDefault.SEX.getName())) {
				sexIndex = headerIndex;
				String sex = getContent(sheet.getRow(rowNum).getCell(headerIndex));
				if (!sex.equals("")) {
					customerResource.setSex(sex);
				}
			} else if (headers.get(headerIndex).equals(
					TableKeywordDefault.BIRTHDAY.getName())) {
				birthdayIndex = headerIndex;
				String birthday = getContent(sheet.getRow(rowNum).getCell(headerIndex));
				if (!birthday.equals("")) {
					customerResource.setBirthdayStr(birthday);
				}
			} else if (headers.get(headerIndex).equals(
					TableKeywordDefault.COMPANY.getName())) {
				companyIndex = headerIndex;
				String companyName = getContent(sheet.getRow(rowNum).getCell(headerIndex));
				if (companyName.equals(""))
					continue;
				Company company = companyEao.getCompanyByName(companyName,
						domain.getId());
				if (company == null) {// 注公司并没有在此处考虑地址 & 电话 两个概念
					company = new Company();
					company.setName(companyName);
					company.setDomain(domain);
					company = (Company) companyEao.update(company);
				}
				// 由于此处还没有存储resource已经存储了company，所以应该不会出错
				customerResource.setCompany(company);// 如果是非客户资源，公司也会被更新
			}
		}

		// 所有上面的Integer都应该是存在的
		allHeaderSet.remove(nameIndex);
		allHeaderSet.remove(sexIndex);
		allHeaderSet.remove(birthdayIndex);
		allHeaderSet.remove(companyIndex);
		/**
		 * =================处理基本字段的存储或则是更新==为节省代码此处外加公司字段 处理完毕==================
		 **/

		/** =================对地址字段处理================== **/
		List<Integer> addressList = new ArrayList<Integer>();
		for (Integer headerIndex : allHeaderSet) {
			if (headers.get(headerIndex).equals(
					TableKeywordDefault.ADDRESS.getName())) {
				addressList.add(headerIndex);
			}
		}
		allHeaderSet.removeAll(addressList);
		// 对所有地址字段进行循环处理
		for (Integer addressIndex : addressList) {
			String addressStr = getContent(sheet.getRow(rowNum).getCell(addressIndex));
			if (addressStr.equals("")) {
				continue;
			}
			Address address = new Address();
			address.setStreet(addressStr);// 目前将地址存储到street属性中
			address.setName(customerResource.getName()); // Note:目前程序只处理地址和姓名两项内容
			address.setCustomerResource(customerResource);
			address = (Address) addressEao.update(address);
			customerResource.getAddresses().add(address);
			customerResource.setDefaultAddress(address);
		}
		/** =================对地址字段处理 处理完毕================== **/

		/** =================对描述信息进行处理================== **/
		// 剩下的表格未处理列都为描述信息
		for (Integer descIndex : allHeaderSet) {
			String key = getContent(sheet.getRow(0).getCell(descIndex));
			String value = getContent(sheet.getRow(rowNum).getCell(descIndex));
			
//			if (value.equals("")) {
//				continue;
//			} //value 为空也同样存储
					
			// 此处添加根据key，value和customerResource的Id值去查找描述信息
			// 如果存在，不重新将描述信息插入到数据库，如果不存在则新建描述信息
			// 此处不考虑因为事务的，同一条记录不同列描述信息重复的情况
			/*Boolean isExistDescription = customerResourceDescriptionEao.isExistDescription(key, value, customerResource.getId(),domain.getId());
			if (isExistDescription) {
				continue;
			} */
			value=value==null?"":value;
			CustomerResourceDescription desc = customerResourceDescriptionEao.getExistDescription(key, value, customerResource.getId(), domain.getId());
			if(desc == null) {
				desc = new CustomerResourceDescription();
				desc.setCreateDate(new Date());
				desc.setLastUpdateDate(new Date());
			} else {
				desc.setLastUpdateDate(new Date());
			}
			desc.setDomain(domain);
			desc.setKey(key);
			desc.setValue(value);
			desc.setCustomerResource(customerResource);
			customerResourceDescriptionEao.update(desc);
		}
		/** =================对描述信息进行处理 处理完毕================== **/

		customerResource.setOwner(user);
		if(customerResource.getCount() == null) {
			customerResource.setCount(0);
		}
		customerResource.setExpireDate(new Date(0));

		// jrh
		resourceMap.remove(customerResource.getId());
		resourceMap.put(customerResource.getId(), customerResource);
		resources.addAll(resourceMap.values());
		return resources; // 返回之后会有事物处理
	}

	/** 解析CSV 头部列的对应关系 */
	private HashMap<Integer, String> parseCsvHeader(String[] headerArray, Domain domain) {
		
		// 标记列号和表格头部的对应关系
		HashMap<Integer, String> headers = new HashMap<Integer, String>();
		
		// 一个包含所有必要字段的List，用来记录是否包含所有的必要表格头
		List<String> requiredHeader = new ArrayList<String>();
		for (TableKeywordDefault keyword : TableKeywordDefault.values()) {
			requiredHeader.add(keyword.getName());
		}
		// 用来记录此域中管理员定义的表格头
		List<String> mgrDefinedHeader = tableKeywordEao.getAllStrByDomain(domain);
		mgrDefinedHeader.addAll(requiredHeader);
		
		// 用来记录不符合条件的标题头
		List<String> badHeaderList = new ArrayList<String>();
		
		// 循环读取每一列的标题头
		for (int i = 0; i < headerArray.length; i++) {
			// 不会出现contents为空的情况
			String header = headerArray[i];
			if (header.equals(""))
				continue;
			
			// 如果Excel表格头部字段是必要的标题头，则从requiredHeader中移除
			if (requiredHeader.contains(header)) {
				headers.put(i, header);
				requiredHeader.remove(header);// 移除用来标记是不是包含全部必要的头部
				continue;
			} else if (!mgrDefinedHeader.contains(header)) {
				badHeaderList.add(header);
				continue;
			} else {// 是管理员定义的Excel表格头
				headers.put(i, header);
			}
		}
		
//		
		
		// 错误消息的字符串
		String errorMsg = "";
		// 必要的标题头没有完全包含错误信息
		if (requiredHeader.size() > 0) {
			String requiredErrorMsg = "必须包含:";
			// 必要的标题头
			StringBuilder sb = new StringBuilder();
			Iterator<String> iter = requiredHeader.iterator();
			while (iter.hasNext()) {
				String tempMsg = iter.next();
				sb.append(tempMsg);
				if (iter.hasNext())
					sb.append(",");
			}
			requiredErrorMsg += sb.toString();
			errorMsg += requiredErrorMsg;
		}
		
		// 标题头不在管理员指定的范围内错误信息
		if (badHeaderList.size() > 0) {
			// 管理员没有指定的表格头
			StringBuilder sb = new StringBuilder();
			Iterator<String> iter = badHeaderList.iterator();
			while (iter.hasNext()) {
				String tempMsg = iter.next();
				sb.append(tempMsg);
				if (iter.hasNext())
					sb.append(",");
			}
			// 全部错误信息
			if (errorMsg.length() > 0) {
				errorMsg += "     "; // 在两条错误信息中间加分隔空字符
			}
			errorMsg += "不能包含:" + sb.toString();
		}
		
		// 如果Excel头部不符合条件，抛出异常
		if (!errorMsg.equals("")) {
			throw new RuntimeException(errorMsg);
		}
		
		checkHeader(headers);
		// 对于Header中的字段进行重新排序
		return headers;
	}
	
	/** 解析表头部列的对应关系 */
	private HashMap<Integer, String> parseExcelHeader(Sheet sheet, Domain domain) {
		if (sheet.getPhysicalNumberOfRows() <= 1) {
			throw new RuntimeException("Excel 文件中的内容为空，或格式不正确！");
		}
		// 标记列号和表格头部的对应关系
		HashMap<Integer, String> headers = new HashMap<Integer, String>();

		// 一个包含所有必要字段的List，用来记录是否包含所有的必要表格头
		List<String> requiredHeader = new ArrayList<String>();
		for (TableKeywordDefault keyword : TableKeywordDefault.values()) {
			requiredHeader.add(keyword.getName());
		}
		// 用来记录此域中管理员定义的表格头
		List<String> mgrDefinedHeader = tableKeywordEao.getAllStrByDomain(domain);
		mgrDefinedHeader.addAll(requiredHeader);

		// 用来记录不符合条件的标题头
		List<String> badHeaderList = new ArrayList<String>();

		// 循环读取每一列的标题头
		for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
			// 不会出现contents为空的情况
			String header = getContent(sheet.getRow(0).getCell(i));
			if (header.equals(""))
				continue;

			// 如果Excel表格头部字段是必要的标题头，则从requiredHeader中移除
			if (requiredHeader.contains(header)) {
				headers.put(i, header);
				requiredHeader.remove(header);// 移除用来标记是不是包含全部必要的头部
				continue;
			} else if (!mgrDefinedHeader.contains(header)) {
				badHeaderList.add(header);
				continue;
			} else {// 是管理员定义的Excel表格头
				headers.put(i, header);
			}
		}
		
//		

		// 错误消息的字符串
		String errorMsg = "";
		// 必要的标题头没有完全包含错误信息
		if (requiredHeader.size() > 0) {
			String requiredErrorMsg = "必须包含:";
			// 必要的标题头
			StringBuilder sb = new StringBuilder();
			Iterator<String> iter = requiredHeader.iterator();
			while (iter.hasNext()) {
				String tempMsg = iter.next();
				sb.append(tempMsg);
				if (iter.hasNext())
					sb.append(",");
			}
			requiredErrorMsg += sb.toString();
			errorMsg += requiredErrorMsg;
		}

		// 标题头不在管理员指定的范围内错误信息
		if (badHeaderList.size() > 0) {
			// 管理员没有指定的表格头
			StringBuilder sb = new StringBuilder();
			Iterator<String> iter = badHeaderList.iterator();
			while (iter.hasNext()) {
				String tempMsg = iter.next();
				sb.append(tempMsg);
				if (iter.hasNext())
					sb.append(",");
			}
			// 全部错误信息
			if (errorMsg.length() > 0) {
				errorMsg += "     "; // 在两条错误信息中间加分隔空字符
			}
			errorMsg += "不能包含:" + sb.toString();
		}

		// 如果Excel头部不符合条件，抛出异常
		if (!errorMsg.equals("")) {
			throw new RuntimeException(errorMsg);
		}

		checkHeader(headers);
		// 对于Header中的字段进行重新排序
		return headers;
	}

	/**
	 * 验证表格头部是否符合规则（姓名、性别、生日、公司唯一）
	 */
	private void checkHeader(HashMap<Integer, String> headers) {
		Boolean isCheckedName = false;
		Boolean isCheckedGender = false;
		Boolean isCheckedBirthday = false;
		Boolean isCheckedCompany = false;
		for (Integer headerKey : headers.keySet()) {
			if (headers.get(headerKey).equals(
					TableKeywordDefault.NAME.getName())) {
				if (isCheckedName == true) {
					throw new RuntimeException("表格头只允许出现一个“姓名”");
				} else {
					isCheckedName = true;
				}
			} else if (headers.get(headerKey).equals(
					TableKeywordDefault.SEX.getName())) {
				if (isCheckedGender == true) {
					throw new RuntimeException("表格头只允许出现一个“性别”");
				} else {
					isCheckedGender = true;
				}
			} else if (headers.get(headerKey).equals(
					TableKeywordDefault.BIRTHDAY.getName())) {
				if (isCheckedBirthday == true) {
					throw new RuntimeException("表格头只允许出现一个“生日”");
				} else {
					isCheckedBirthday = true;
				}
			} else if (headers.get(headerKey).equals(
					TableKeywordDefault.COMPANY.getName())) {
				if (isCheckedCompany == true) {
					throw new RuntimeException("表格头只允许出现一个“公司”");
				} else {
					isCheckedCompany = true;
				}
			}
		}
	}

	/**
	 * 将电话号码前的0去除，并去掉电话号码中间的-符号
	 * 
	 * @param contents
	 * @return
	 */
	private String phoneNumberProcess(String phoneNumber) {
		// 前面的Value部分已经Trim过了，不用再次进行处理
		if (phoneNumber == null || phoneNumber.equals("")) {
			return phoneNumber;
		}
		// 去掉电话号码中的非数字字符
		char[] vala = phoneNumber.toCharArray();
		phoneNumber = "";
		// Ascii的对应关系是0-48 9-57
		for (char charElement : vala) {
			// 如果是数字
			if (charElement > 47 && charElement < 58) {
				phoneNumber += charElement;
			}
		}
//		// 去掉电话号码前面的0
//		int len = phoneNumber.length();
//		int st = 0;
//		char[] valb = phoneNumber.toCharArray();
//		while ((st < len) && (valb[st] == '0')) {
//			st++;
//		}
//		return phoneNumber.substring(st, len);
		return phoneNumber;
	}

	// //////////////////////////////////////////////////////

	// getter and setter
	public CustomerResourceBatchEao getCustomerResourceBatchEao() {
		return customerResourceBatchEao;
	}

	public void setCustomerResourceBatchEao(
			CustomerResourceBatchEao customerResourceBatchEao) {
		this.customerResourceBatchEao = customerResourceBatchEao;
	}

	public TelephoneEao getTelephoneEao() {
		return telephoneEao;
	}

	public void setTelephoneEao(TelephoneEao telephoneEao) {
		this.telephoneEao = telephoneEao;
	}

	public CompanyEao getCompanyEao() {
		return companyEao;
	}

	public void setCompanyEao(CompanyEao companyEao) {
		this.companyEao = companyEao;
	}

	public TableKeywordEao getTableKeywordEao() {
		return tableKeywordEao;
	}

	public void setTableKeywordEao(TableKeywordEao tableKeywordEao) {
		this.tableKeywordEao = tableKeywordEao;
	}

	public CustomerResourceEao getCustomerResourceEao() {
		return customerResourceEao;
	}

	public void setCustomerResourceEao(CustomerResourceEao customerResourceEao) {
		this.customerResourceEao = customerResourceEao;
	}

	public CustomerResourceDescriptionEao getCustomerResourceDescriptionEao() {
		return customerResourceDescriptionEao;
	}

	public void setCustomerResourceDescriptionEao(
			CustomerResourceDescriptionEao customerResourceDescriptionEao) {
		this.customerResourceDescriptionEao = customerResourceDescriptionEao;
	}

	public AddressEao getAddressEao() {
		return addressEao;
	}

	public void setAddressEao(AddressEao addressEao) {
		this.addressEao = addressEao;
	}

	public MarketingProjectTaskEao getMarketingProjectTaskEao() {
		return marketingProjectTaskEao;
	}

	public void setMarketingProjectTaskEao(
			MarketingProjectTaskEao marketingProjectTaskEao) {
		this.marketingProjectTaskEao = marketingProjectTaskEao;
	}

	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}

	@Override
	@Transactional
	public List<CustomerResource> persistOneRow(jxl.Sheet sheet,
			HashMap<Integer, String> headers, int row, Boolean isAppend,
			CustomerResourceBatch batch, User user, Map<String, Long> resultMap,Boolean quChong) {
		//jxl sheet
		return null;
	}

	@Override
	public String importOneCustomer(boolean isAppend, CustomerResourceBatch resourceBatch, CustomerResource customer, 
			HashMap<String, String> descriptionMap, String phonenum, String companyName, String addressInfo) {
		
		if(resourceBatch == null || customer == null || StringUtils.isEmpty(phonenum)) {
			return "failed cause by : Incomplete information!][请求失败！原因：信息不完整] ";
		}
		
		Domain domain = customer.getDomain();
		if(customer.getId() == null) {	// 如果该客户尚未存储到数据库，则创建资源和电话对象
			customer = (CustomerResource) customerResourceEao.update(customer);	// 先对资源进行存储
			
			Telephone telephone = new Telephone();
			telephone.setNumber(phonenum);
			telephone.setDomain(domain);
			telephone.setCustomerResource(customer);
			telephone = (Telephone) telephoneEao.update(telephone); 

			customer.getTelephones().add(telephone);
		}
		
		boolean isUncustomer = (customer.getAccountManager() == null) ? true : false;	// 当前资源是否为成功客户
		
		/* 处理公司信息 */
		if(isUncustomer && companyName != null && !"".equals(companyName)) {	// 只要当前资源费成功客户时，才需要更新其基础信息
			Company company = companyEao.getCompanyByName(companyName, domain.getId());
			if (company == null) {// 注公司并没有在此处考虑地址 & 电话 两个概念
				company = new Company();
				company.setName(companyName);
				company.setDomain(domain);
				company = (Company) companyEao.update(company);
			} else {
				customer.setCompany(company);// 如果是非客户资源，公司也会被更新
			}
		}

		/* 处理地址信息 */
		if(isUncustomer && addressInfo != null && !"".equals(addressInfo)) {	// 只要当前资源费成功客户时，才需要更新其基础信息
			boolean isnewAddr = true;
			for(Address addr : customer.getAddresses()) {
				if(addr.getStreet().equals(addressInfo)) {
					isnewAddr = false;
					break;
				}
			}
			
			if(isnewAddr) {	// 如果是新地址，则创建一个地址对象
				Address address = new Address();
				address.setStreet(addressInfo);	
				address.setName(customer.getName()); 	
				address.setCustomerResource(customer);
				address = (Address) addressEao.update(address);
				customer.getAddresses().add(address);
				if(customer.getDefaultAddress() == null) {	// 如果该客户没有默认地址，则将当前地址置为默认
					customer.setDefaultAddress(address);
				}
			}
		}
		
		/* 处理客户的描述信息 */
		for(String key : descriptionMap.keySet()) {
			String value = descriptionMap.get(key);
			
			// 根据key，value和customerResource的Id值去查找描述信息, 如果存在，不重新将描述信息插入到数据库，如果不存在则新建描述信息
			/*Boolean isExistDescription = customerResourceDescriptionEao.isExistDescription(key, value, customer.getId(), domain.getId());	
			if (isExistDescription) {
				continue;
			} */
			value=value==null?"":value;
			CustomerResourceDescription description = customerResourceDescriptionEao.getExistDescription(key, value, customer.getId(), domain.getId());
			if(description == null) {
				description = new CustomerResourceDescription();
				description.setCreateDate(new Date());
				description.setLastUpdateDate(new Date());
			} else {
				description.setLastUpdateDate(new Date());
			}
			description.setDomain(domain);
			description.setKey(key);
			description.setValue(value);
			description.setCustomerResource(customer);
			customerResourceDescriptionEao.update(description);
		}
		
		customer.getCustomerResourceBatches().add(resourceBatch);
		customer = (CustomerResource) customerResourceEao.update(customer);
		
		if(isAppend) {	// 如果是追加资源，则需要更新项目(使用了当前批次的项目)下的任务
			checkAndPersistTask(resourceBatch, customer, domain);
		}
		
		return "success! [推送成功]";
	}

	/**
	 * @Description 描述：将操作数据库的代码写在这里不好，但原来的EAO调用将导致一个事务被分成多个，所以暂且这样吧
	 *		作用：
	 *			检查当前批次是否被项目使用，如果是，则在这些项目中创建该客户对应的任务			
	 *
	 * @author  JRH
	 * @date    2014年12月9日 下午5:03:38
	 * @param resourceBatch	批次
	 * @param customer		客户
	 * @param domain 		租户
	 */
	@SuppressWarnings("unchecked")
	private void checkAndPersistTask(CustomerResourceBatch resourceBatch, CustomerResource customer, Domain domain) {
		EntityManager em = commonEao.getEntityManager();
		
		// 通过批次取得对应n个项目的Id值
		String nativeSql = "select marketingproject_id from ec2_markering_project_ec2_customer_resource_batch where batches_id=" + resourceBatch.getId();
		List<Long> projectIdList = ((List<Long>) commonEao.excuteNativeSql(nativeSql, ExecuteType.RESULT_LIST));
		Set<Long> projectIds = new HashSet<Long>(projectIdList);			//此处多加了一个Set目的是节省查询的资源，不使用distinct
		Boolean isValidTask = (resourceBatch.getBatchStatus() == BatchStatus.USEABLE) ? true : false;
		
		for (Long projectId : projectIds) {
			MarketingProject project = (MarketingProject) commonEao.excuteSql("select mp from MarketingProject as mp where mp.id = "+projectId, ExecuteType.SINGLE_RESULT);
			
			if(project != null) {
				MarketingProjectType projectType = project.getMarketingProjectType();		// 任务的类型跟项目类型相同
				MarketingProjectTaskType taskType = MarketingProjectTaskType.MARKETING;
				int projectTypeIndex = (projectType != null) ? projectType.getIndex() : 0;
				if(projectTypeIndex == MarketingProjectTaskType.MARKETING.getIndex()) {
					taskType = MarketingProjectTaskType.MARKETING;
				} else if(projectTypeIndex == MarketingProjectTaskType.QUESTIONNAIRE.getIndex()) {
					taskType = MarketingProjectTaskType.QUESTIONNAIRE;
				} else if(projectTypeIndex == MarketingProjectTaskType.CALL_BACK.getIndex()) {
					taskType = MarketingProjectTaskType.CALL_BACK;
				}
				
				StringBuffer nativeCountSqlStrBf = new StringBuffer();
				nativeCountSqlStrBf.append("select count(*) from ec2_marketing_project_task where customerresource_id = ");
				nativeCountSqlStrBf.append(customer.getId());
				nativeCountSqlStrBf.append(" and marketingproject_id = ");
				nativeCountSqlStrBf.append(projectId);
				nativeCountSqlStrBf.append(" and marketingprojecttasktype = ");
				nativeCountSqlStrBf.append(projectTypeIndex);
				nativeCountSqlStrBf.append(" and domain_id = ");
				nativeCountSqlStrBf.append(domain.getId());
				
				Long count = (Long) em.createNativeQuery(nativeCountSqlStrBf.toString()).getSingleResult();	// 任务数量

				if (count >= 1) {	// 说明已经存储一个相应的Task，不用重复存储,正常最大值为1
					logger.info("JRH--> 项目Id为 "+projectId+" 的项目中已经包含id 值为 "+customer.getId()+" 的资源所对应的任务！");
				} else {
					MarketingProjectTask task = new MarketingProjectTask();
					task.setCreateTime(new Date());
					task.setIsAnswered(false);
					task.setIsFinished(false);
					task.setIsUseable(isValidTask);
					task.setCustomerResource(customer);
					task.setBatchId(resourceBatch.getId());
					task.setMarketingProject(project);
					task.setMarketingProjectTaskType(taskType);
					task.setDomain(domain); 
					if(MarketingProjectTaskType.QUESTIONNAIRE.equals(taskType)) {
						task.setCustomerQuestionnaireFinishStatus(CustomerQuestionnaireFinishStatus.UN_STARTED);
					}
					
					try {
						commonEao.save(task);
					} catch (Exception e) {
						logger.info("JRH--> 项目中已经包含此条资源！");
					}
				}
			}
		}
		
	}
	
	@Override
	public String importOneCustomer(boolean isAppend, CustomerResourceBatch resourceBatch, CustomerResource customer, 
			HashMap<String, String> descriptionMap, List<Telephone> telephonesList, List<Address> addressesList, String companyName) {
		
		if(resourceBatch == null || customer == null || telephonesList == null || telephonesList.size() == 0) {
			return "failed cause by : Incomplete information!][请求失败！原因：信息不完整] ";
		}
		
		Domain domain = customer.getDomain();
		if(domain == null) {
			domain = customer.getOwner().getDomain();
		}
		if(customer.getId() == null) {	// 如果该客户尚未存储到数据库，则创建资源和电话对象
			customer = (CustomerResource) customerResourceEao.update(customer);	// 先对资源进行存储
			for(Telephone telephone : telephonesList) {
				telephone.setNumber(telephone.getNumber());
				telephone.setDomain(domain);
				telephone.setCustomerResource(customer);
				telephone = (Telephone) telephoneEao.update(telephone); 
	
				customer.getTelephones().add(telephone);
			}
			
		} else {
			customer = (CustomerResource) customerResourceEao.update(customer);	// 先对资源进行存储
			List<String> phoneNumbersList = new ArrayList<String>();
			for(Telephone phone : customer.getTelephones()) {
				phoneNumbersList.add(phone.getNumber());
			}
			for(Telephone telephone : telephonesList) {
				if(!phoneNumbersList.contains(telephone.getNumber())) {
					telephone.setNumber(telephone.getNumber());
					telephone.setDomain(domain);
					telephone.setCustomerResource(customer);
					telephone = (Telephone) telephoneEao.update(telephone); 
					customer.getTelephones().add(telephone);
				}
			}
		}
		
		/**
		 * 如果为成功客户应该是就不更新这条客户资源的其他信息
		 */
		boolean isUncustomer = (customer.getAccountManager() == null) ? true : false;	// 当前资源是否为成功客户
		
		/* 处理公司信息 */
		if(isUncustomer && companyName != null && !"".equals(companyName)) {	// 只要当前资源费成功客户时，才需要更新其基础信息
			Company company = companyEao.getCompanyByName(companyName, domain.getId());
			if (company == null) {// 注公司并没有在此处考虑地址 & 电话 两个概念
				company = new Company();
				company.setName(companyName);
				company.setDomain(domain);
				company = (Company) companyEao.update(company);
			} else {
				customer.setCompany(company);// 如果是非客户资源，公司也会被更新
			}
		}

		/* 处理地址信息 */
		if(isUncustomer && addressesList != null && addressesList.size() > 0) {	// 只要当前资源费成功客户时，才需要更新其基础信息
			boolean isnewAddr = true;
			for(Address address : addressesList) {
				for(Address addr : customer.getAddresses()) {
					if(addr.getStreet().equals(address.getStreet())) {
						isnewAddr = false;
						break;
					}
				}
				
				if(isnewAddr) {	// 如果是新地址，则创建一个地址对象
					address.setStreet(address.getStreet());	
					address.setName(customer.getName()); 	
					address.setCustomerResource(customer);
					address = (Address) addressEao.update(address);
					customer.getAddresses().add(address);
					if(customer.getDefaultAddress() == null) {	// 如果该客户没有默认地址，则将当前地址置为默认
						customer.setDefaultAddress(address);
					}
				}
				
			}
			
		}
		
		/* 处理客户的描述信息 */
		for(String key : descriptionMap.keySet()) {
			String value = descriptionMap.get(key);
			
			// 根据key，value和customerResource的Id值去查找描述信息, 如果存在，不重新将描述信息插入到数据库，如果不存在则新建描述信息
			/*Boolean isExistDescription = customerResourceDescriptionEao.isExistDescription(key, value, customer.getId(), domain.getId());	
			if (isExistDescription) {
				continue;
			} */
			value=value==null?"":value;
			CustomerResourceDescription description = customerResourceDescriptionEao.getExistDescription(key, value, customer.getId(), domain.getId());
			if(description == null) {
				description = new CustomerResourceDescription();
				description.setCreateDate(new Date());
				description.setLastUpdateDate(new Date());
			} else {
				description.setLastUpdateDate(new Date());
			}
			description.setDomain(domain);
			description.setKey(key);
			description.setValue(value);
			description.setCustomerResource(customer);
			customerResourceDescriptionEao.update(description);
		}
		
		customer.getCustomerResourceBatches().add(resourceBatch);
		customer = (CustomerResource) customerResourceEao.update(customer);
		
		if(isAppend) {	// 如果是追加资源，则需要更新项目(使用了当前批次的项目)下的任务
			checkAndPersistTask(resourceBatch, customer, domain);
		}
		
		return "success! [推送成功]";
	}
	
}
