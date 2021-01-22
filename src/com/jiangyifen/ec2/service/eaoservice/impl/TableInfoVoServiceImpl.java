package com.jiangyifen.ec2.service.eaoservice.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import com.jiangyifen.ec2.eao.TableInfoVoEao;
import com.jiangyifen.ec2.service.eaoservice.TableInfoVoService;
import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.TableInfoVo;

/**
 * Service实现类：数据表信息
 * 
 * @author JHT
 */
public class TableInfoVoServiceImpl implements TableInfoVoService {
	
	private DecimalFormat df = new DecimalFormat("0.00"); // 只显示小数点后2位
	
	private TableInfoVoEao tableInfoVoEao;
	
	// 获取所有表的大小以及当前索引值
	@Override
	public List<TableInfoVo> getTableInfoVoList(String tableName) {
		List<TableInfoVo> tableInfoVoList = new ArrayList<TableInfoVo>(); // 定义一个用来存储表信息对象的集合
		
		Map<String, String> tabInfoMap = findMapByTableName(tableName);	// 1. 获取带有表名,序列名的Map结合
		TableInfoVo tableInfoVo = null;
		
		for (Entry<String, String> entry : tabInfoMap.entrySet()) {	// 2. 获取Map的键值信息,进行查询出数据库表的大小和序列的当前值
			tableInfoVo = new TableInfoVo();
			tableInfoVo.setRelName(entry.getKey());
			try {
				long tableSize =  findSizeByTableName(entry.getKey());	//1.计算大小
				tableInfoVo.setDataNum(tableSize);
			} catch (Exception e) {}

			if (StringUtils.isNotEmpty(entry.getValue())) {	//2.计算MaxId
				try {
					long maxId = findMaxIdByTableName(entry.getValue());
					tableInfoVo.setSeqMax(maxId);
				} catch (Exception e) {
					tableInfoVo.setSeqMax(-1);
				}
			}
			
			tableInfoVoList.add(tableInfoVo);
		}
		
		// 根据序列值进行倒序排列
		Collections.sort(tableInfoVoList);
		Collections.reverse(tableInfoVoList);
		return tableInfoVoList;
	}

	// 查询出数据库表名称,和序列名称的Map<String,String>的集合
	private Map<String, String> findMapByTableName(String tableName) {
		Map<String, String> tableInfoMap = new HashMap<String, String>(); // 用来存放数据库表名称和序列名称的Map集合
		List<String> tableNameList = new ArrayList<String>(); // 用来存放表名称的集合
		List<String> seqNameList = new ArrayList<String>(); // 用来存放序列名称的集合
		StringBuilder sqlNameBuilder = new StringBuilder("select relname from pg_stat_user_tables where schemaname='public' "); // 用来查询出所有的表名称
		StringBuilder sqlSeqBuilder = new StringBuilder("select relname from pg_class where relname like 'seq_ec2%' "); // 用来查询出序列的名称
		if (tableName != null && tableName.trim().length() > 0) {
			sqlNameBuilder.append(" and relname like '%" + tableName + "%' ");
		}

		// 获取数据
		tableNameList = tableInfoVoEao.getTableNameListOrSeqNameList(sqlNameBuilder.toString());
		seqNameList = tableInfoVoEao.getTableNameListOrSeqNameList(sqlSeqBuilder.toString());
		seqNameList = addSeqMaxsList(seqNameList);

		// 循环遍历出数据存入到指定的Map集合中
		for (String tbName : tableNameList) {
			for (String seqName : seqNameList) { // 进行遍历出数据库中所有的序列名称
				if (seqName.equals("seq_" + tbName + "_id")) { // 比较当前表的名称是否和序列名称相同,如果相同,则跳出这一层for循环
					tableInfoMap.put(tbName, seqName);
					break;
				} else if (seqName.equals(tbName + "_id")) {
					tableInfoMap.put(tbName, seqName);
					break;
				} else if (seqName.equals(tbName)) {
					tableInfoMap.put(tbName, seqName);
					break;
				} else if (tbName.equals("ec2_sip_conf")) { // 特殊情况,这个表名和序列名有点不匹配
					tableInfoMap.put(tbName, "seq_ec2_sip_config_id");
					break;
				}
			}
			if (!tableInfoMap.containsKey(tbName)) { // Map集合中不存在这个表名称的键的信息,那么则添加进去
				tableInfoMap.put(tbName, "");
			}
		}

		return tableInfoMap;
	}

	// 由于有些序列名称不是以seq_ec2开头的 所以这里要自己手动添加进去
	private List<String> addSeqMaxsList(List<String> seqMaxsList) {
		seqMaxsList.add("ec2_dst_order_log_id");
		seqMaxsList.add("project_task_count");
		seqMaxsList.add("seq_cdr_id");
		seqMaxsList.add("seq2_cdr_id");
		seqMaxsList.add("seq_cdreventlogger_id");
		seqMaxsList.add("seq_customer_satisfaction_investigation_log_id");
		seqMaxsList.add("seq_user_exten_persist_id");
		seqMaxsList.add("vsj_log_id");
		return seqMaxsList;
	}

	// 根据表名称查询表的大小
	private long findSizeByTableName(String tableName) {
		String sql = "select pg_relation_size(relid) from pg_stat_user_tables where schemaname='public' and relname='" + tableName + "' ";
		return tableInfoVoEao.getTableSizeOrMaxId(sql);
	}

	/**
	 * 根据表名称查询序列的当前值
	 * @param tableName
	 * @return
	 */
	private long findMaxIdByTableName(String seqName) {
		String sql = "select last_value from " + seqName;
		return tableInfoVoEao.getTableSizeOrMaxId(sql);
	}

	// 用来计算总数据的大小
	@Override
	public String getSumTableInfoSize() {
		String sql = "select sum(pg_relation_size(relid)) from pg_stat_user_tables where schemaname='public'";
		long sumTableSizeObject = tableInfoVoEao.getTableSizeOrMaxId(sql);
		Double sumTableSize = 0.0;
		sumTableSize = (double) sumTableSizeObject;
		return df.format(sumTableSize / 1024 / 1024).toString();
	}

	// 用来计算表的总个数
	@Override
	public int getTableInfoCount() {
		String sql = "select count(relname) from pg_stat_user_tables where schemaname='public'";
		return Integer.parseInt(String.valueOf(tableInfoVoEao.getTableSizeOrMaxId(sql)));
	}

	public TableInfoVoEao getTableInfoVoEao() {
		return tableInfoVoEao;
	}

	public void setTableInfoVoEao(TableInfoVoEao tableInfoVoEao) {
		this.tableInfoVoEao = tableInfoVoEao;
	}

	
	@Override
	public long getTableColumnCountByTableName(String tableName) {
		if(StringUtils.isNotEmpty(tableName)){
			String sql = "select count(*) from "+tableName+" ";
			return tableInfoVoEao.getTableSizeOrMaxId(sql);
		}
		return -1L;
	}

}