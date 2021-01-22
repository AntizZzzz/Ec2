package com.jiangyifen.ec2.servlet.http.common.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.servlet.http.common.pojo.DataCommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.FastJsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：调用该接口，用来查找 CDR 呼叫记录
 * 
 * 可以根据传入的参数，来查询 CDR 呼叫记录：
 * 	1、page 当前页
 * 	2、limit 每页显示页数
 * 	3、username 用户名
 * 	4、isBridged 是否接通，true 已接通，false 和 null 为未接通
 * 	5、idSort 根据 ID 进行排序，结果为 asc 或者 desc
 *  
 *  6、dialingNo 主叫号码
 *  7、calledNo 被叫号码
 *  8、bridgedTimeMoreThan 通话时长大于等于填写项
 *  9、bridgedTimeLessThan 通话时长小于等于填写项
 *  10、startTime 联系开始时间
 *  11、finishTime 联系截止时间
 *  12、uniqueId 通话记录唯一标识
 *
 * 返回结果：
 * 	1、code 0 表示调用结果成功，-1 表示调用结果失败
 *  2、message 成功或失败的信息
 *  3、totalCount 总记录条数
 *  4、results 返回结果
 *
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/data/acquireCdr?accessId=xxx&accessKey=xxx&username=1001&page=1&limit=10&isBridged=false&idSort=desc&dialingNo=主叫号码&calledNo=被叫号码&bridgedTimeMoreThan=整型值&bridgedTimeLessThan=整型值&startTime=yyyy-MM-dd HH:mm:ss&finishTime=yyyy-MM-dd HH:mm:ss
 *
 * eg.
 *	http://192.168.1.160:8088/ec2/http/common/data/acquireCdr?accessId=xxx&accessKey=xxx&username=1001&page=1&limit=10&isBridged=false&idSort=desc&dialingNo=800001&calledNo=0123456789121&bridgedTimeMoreThan=0&bridgedTimeLessThan=3&startTime=2015-06-01 00:00:00&finishTime=2015-06-03 00:00:00
 *
 * @author jinht
 *
 * @date 2015-6-4 上午9:03:46 
 *
 */
@SuppressWarnings("serial")
public class AcquireCdrServlet extends HttpServlet {

	// 日志工具类
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CdrService cdrService = SpringContextHolder.getBean("cdrService");
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataCommonRespBo dataCommonRespBo = new DataCommonRespBo();		// 响应信息
		dataCommonRespBo.setCode(0);
		dataCommonRespBo.setMessage("获取呼叫记录信息成功");
	
		String jpqlSearch;
		try {
			String username = StringUtils.trimToEmpty(request.getParameter("username"));									// 获取用户名
			Integer page = request.getParameter("page") == null ? null : Integer.valueOf(request.getParameter("page"));	// 当前页
			Integer limit = request.getParameter("limit") == null ? null : Integer.valueOf(request.getParameter("limit"));	// 每页显示行数
			String idSort = StringUtils.trimToEmpty(request.getParameter("idSort"));										// 排序 
			String isBridged = StringUtils.trimToEmpty(request.getParameter("isBridged"));									// 接通情况
			String uniqueId = StringUtils.trimToEmpty(request.getParameter("uniqueId"));									// uniqueId
			
			/**
			 * 扩展接口字段
			 */
			String dialingNo = StringUtils.trimToEmpty(request.getParameter("dialingNo"));							// 主叫号码
			String calledNo = StringUtils.trimToEmpty(request.getParameter("calledNo"));							// 被叫号码
			String bridgedTimeMoreThan = StringUtils.trimToEmpty(request.getParameter("bridgedTimeMoreThan"));		// 通话时长大于等于填写项
			String bridgedTimeLessThan = StringUtils.trimToEmpty(request.getParameter("bridgedTimeLessThan"));		// 通话时长小于等于填写项
			String startTime = StringUtils.trimToEmpty(request.getParameter("startTime"));							// 联系开始时间
			String finishTime = StringUtils.trimToEmpty(request.getParameter("finishTime"));						// 联系截止时间
			
			if(page == null && limit != null) {	// 如果每页显示的行数为空，页数不为空，则把页数默认设置为第 1 页
				page = 1;
			}
			
			if(page != null && page < 1) {		// 分页
				dataCommonRespBo.setCode(-2);
				dataCommonRespBo.setMessage("获取呼叫记录信息失败，原因：page 值是获取第几页，所以不能为小于 1 的整数!");
				operateResponse(response, dataCommonRespBo);
				logger.warn("jinht -->> 调用了查找 CDR 记录的接口，获取呼叫记录信息失败，原因：page 值是获取第几页，所以不能为小于 1 的整数!");
				return;
			}
			
			if("".equals(idSort)) {			// 如果没有填写根据id字段的排序方法，则默认为降序排列
				idSort = "desc";
			}
			
			/*StringBuffer sbJpql = new StringBuffer("select count(s.id) from Cdr as s where 1=1 ");*/
			StringBuffer sbJpql = new StringBuffer("select count(s.id) from Cdr as s ");
			if(!"".equals(username)) {		// 根据用户名进行查询
				sbJpql.append(" and (s.srcUsername = '"+username+"' or s.destUsername = '"+username+"') ");
			}
			
			if(!"".equals(isBridged)) {		// 接通情况
				if("true".equals(isBridged)) {
					sbJpql.append(" and s.isBridged = '"+isBridged+"'");
				} else if("false".equals(isBridged)) {
					sbJpql.append(" and s.isBridged is null ");
				}
			}
			
			if(!"".equals(uniqueId)) {		// 录音文件唯一标识
				sbJpql.append(" and s.uniqueId = '");
				sbJpql.append(uniqueId);
				sbJpql.append("' ");
			}
			
			if(!"".equals(dialingNo)) {		// 主叫号码
				sbJpql.append(" and s.src = '");
				sbJpql.append(dialingNo);
				sbJpql.append("'");
			}
			
			if(!"".equals(calledNo)) {		// 被叫号码
				sbJpql.append(" and s.destination = '"+calledNo+"'");
			}
			
			if(!"".equals(bridgedTimeMoreThan)) {		// 通话时长大于等于填写项
				sbJpql.append(" and s.ec2_billableSeconds >= " + bridgedTimeMoreThan);
			}
			
			if(!"".equals(bridgedTimeLessThan)) {		// 通话时长小于等于填写项
				sbJpql.append(" and s.ec2_billableSeconds <= " + bridgedTimeLessThan);
			}
			
			if(!"".equals(startTime)) {			// 联系开始时间
				sbJpql.append(" and s.startTimeDate >= '" + startTime + "'");
			}
			
			if(!"".equals(finishTime)) {		// 联系截止时间
				sbJpql.append(" and s.startTimeDate <= '" + finishTime + "'");
			}
			
			String jpqlCount = sbJpql.toString().replaceFirst("and", "where");
			int totalCount = cdrService.getEntityCount(jpqlCount);	// 总记录条数
			
			dataCommonRespBo.setTotalCount(totalCount);
			
			jpqlSearch = jpqlCount.replace("count(s.id)", "s") + " order by s.id "+idSort;
			
			List<Cdr> cdrList = new ArrayList<Cdr>();
			
			if(page == null && limit == null) {
				cdrList = cdrService.getCdrByJpql(jpqlSearch);
			} else {
				cdrList = cdrService.loadPageEntities(((page-1) * limit), limit, jpqlSearch);
			}
			
			dataCommonRespBo.setResults(cdrList);
			
			operateResponse(response, dataCommonRespBo);
			
			logger.info("jinht -->> 调用了查找 CDR 记录的接口，查询条件执行的 SQL 语句为："+jpqlSearch);
		} catch (Exception e) {
			dataCommonRespBo.setCode(-1);
			dataCommonRespBo.setMessage("获取呼叫记录信息失败，原因："+e.getMessage());
			operateResponse(response, dataCommonRespBo);
			logger.error("jinht -->> 调用了查找 CDR 记录的接口，获取呼叫记录信息失败，原因："+e.getMessage(), e);
		}
		
	}
	
	/**
	 * @Description 描述：返回操作的反馈信息
	 *
	 * @author  JRH
	 * @date    2014年8月8日 下午12:43:21
	 * @param response			HttpServletResponse
	 * @param commonRespBo		响应信息
	 * @throws IOException 
	 */
	private void operateResponse(HttpServletResponse response, DataCommonRespBo dataCommonRespBo) throws IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		if("false".equals(AnalyzeIfaceJointUtil.WHETHER_SUPPORT_CORS)) {
			response.setContentType("text/plain");
			out.println(""+FastJsonUtil.toJson(dataCommonRespBo));
		} else {
			response.setContentType(AnalyzeIfaceJointUtil.RESPONSE_CONTENT_TYPE);
			out.println("callback(" + FastJsonUtil.toJson(dataCommonRespBo) + ")");
		}
		out.close();
	}
	
}
