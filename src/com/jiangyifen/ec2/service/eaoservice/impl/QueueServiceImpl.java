package com.jiangyifen.ec2.service.eaoservice.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.eao.QueueEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.utils.Config;

public class QueueServiceImpl implements QueueService {
	// 日志创建工具
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private QueueEao queueEao;
	
	// ========  enhanced method  ========//
	
	@Override
	public List<Queue> getAllByDomain(Domain domain) {
		return queueEao.getAllByDomain(domain);
	}
	
	@Override
	public List<Queue> getAllByDomain(Domain domain, boolean isnotAutoDial) {
		return queueEao.getAllByDomain(domain.getId(), isnotAutoDial);
	}

	public List<Queue> getAllByDomain(Long domainId, boolean isnotAutoDial) {
		return queueEao.getAllByDomain(domainId, isnotAutoDial);
	}
	
	@Override
	public boolean existByName(String name, Domain domain) {
		return queueEao.existByName(name, domain);
	}
	
//	@Override
//	public boolean existByName(String name) {
//		return queueEao.existByName(name);
//	}
	
	@Override
	public List<Queue> getAllByMusicOnHold(MusicOnHold moh, Domain domain) {
		return queueEao.getAllByMusicOnHold(moh, domain);
	}
	
	@Override
	public Queue getDefaultQueueByDomain(Long domainId) {
		return queueEao.getDefaultQueueByDomain(domainId);
	}
	
	@Override
	public boolean updateAsteriskQueueFile(Domain domain) {
		String srcFileName = Config.props.getProperty(Config.CONF_FILE_PATH) +"queues_domain_"+ domain.getName();
		String dstFileName = Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH) +"queues_domain_"+ domain.getName();
		
		// 如果backup 备份文件夹不存在，则创建文件夹
		File file = new File(Config.props.getProperty(Config.CONF_BACKUP_FILE_PATH));
		if(!file.exists()) {
			file.mkdir();
		}
		
		List<Queue> queues = queueEao.getAllByDomain(domain);
		boolean isSuccess = createFileContent(srcFileName, dstFileName, queues);
		return isSuccess;
	}
	
	/**
	 * 将源文件备份，并根据获取的分机或外线集合，创建新的配置文件
	 * @param srcFileName	原文件
	 * @param dstFileName	新文件
	 * @param queues		队列集合
	 * @return boolean		创建是否成功
	 */
	private boolean createFileContent(String srcFileName, String dstFileName,
			List<Queue> queues) {
		File source = new File(srcFileName+ ".conf");
		File dst = new File(dstFileName+ ".conf.backup");
		
		// 删除原有的备份文件
		if(dst.exists()) {
			dst.delete();
		}
		
		// 创建新的备份文件
		source.renameTo(dst);
		
		// 创写新的sip.conf 文件
		boolean isSuccess = false;
		FileOutputStream fos = null;
		BufferedOutputStream bw = null;
		try {
			fos = new FileOutputStream(source);
			bw = new BufferedOutputStream(fos);
			StringBuffer queuescontent = new StringBuffer();
			
			queuescontent.append("\r\n\r\n\r\n");
			queuescontent.append(";;;;;;;;;;;;;;; custom queues.conf ;;;;;;;;;;;;;;\r\n");
			queuescontent.append("\r\n\r\n\r\n");
			
			for(Queue queue : queues) {
				queuescontent.append(analysisObject("name", queue.getName()));
				queuescontent.append(analysisObject("musiconhold", queue.getMusiconhold().getName()));
				queuescontent.append(analysisObject("timeout", queue.getTimeout()));
				queuescontent.append(analysisObject("monitor-format", queue.getMonitor_format()));
//				queuescontent.append(analysisObject("announce-round-seconds", queue.getAnnounce_round_seconds()));
				queuescontent.append(analysisObject("wrapuptime", queue.getWrapuptime()));
				queuescontent.append(analysisObject("maxlen", queue.getMaxlen()));
				queuescontent.append(analysisObject("strategy", queue.getStrategy()));
				queuescontent.append(analysisObject("joinempty", queue.getJoinempty()));
				queuescontent.append(analysisObject("setinterfacevar", queue.getSetinterfacevar()));
				queuescontent.append(analysisObject("autopause", queue.getAutopause()));
				
				
//				TODO change 写到界面上控制，不要写死在这
				queuescontent.append(analysisObject("autofill", "yes"));
				queuescontent.append(analysisObject("retry", "1"));
				queuescontent.append(analysisObject("ringinuse", "no"));
				
				// JRH 2014-10-18 呼入队列时，如果队列中所有成员正忙，则播放提示音
				queuescontent.append(analysisObject("announce-frequency", queue.getAnnounce_frequency()));
				queuescontent.append(analysisObject("announce-position", "no"));	// 这里目前必须为 no,如果改成yes,则需要修改queue-youarenext、queue-thereare 、 queue-callswaiting 、queue-holdtime 、queue-minute 、queue-minutes 、queue-seconds 、queue-reporthold 、periodic-announce 这些参数对应的英文语音文件
				queuescontent.append(analysisObject("queue-thankyou", "queue_member_busy_hold"));
				
				/**
				 * @changelog 2014-6-17 下午1:36:16 chenhb <p>description:修改队列的允许进入条件 </p>
				 */
				/*queuescontent.append(analysisObject("joinempty", "strict"));
				queuescontent.append(analysisObject("leavewhenempty", "strict"));*/
				
//				queuescontent.append(analysisObject("dynamicmember", queue.getDynamicmember()));
//				queuescontent.append(analysisObject("announce", queue.getAnnounce()));
//				queuescontent.append(analysisObject("announce-holdtime", queue.getAnnounce_holdtime()));
//				queuescontent.append(analysisObject("context", queue.getContext()));
//				queuescontent.append(analysisObject("eventmemberstatus", queue.getEventmemberstatus()));
//				queuescontent.append(analysisObject("eventwhencalled", queue.getEventwhencalled()));
//				queuescontent.append(analysisObject("leavewhenempty", queue.getLeavewhenempty()));
//				queuescontent.append(analysisObject("memberdelay", queue.getMemberdelay()));
//				queuescontent.append(analysisObject("monitor-join", queue.getMonitor_join()));
//				queuescontent.append(analysisObject("periodic-announce-frequency", queue.getPeriodic_announce_frequency()));
//				queuescontent.append(analysisObject("queue-youarenext", queue.getQueue_youarenext()));
//				queuescontent.append(analysisObject("queue-thereare", queue.getQueue_thereare()));
//				queuescontent.append(analysisObject("queue-callswaiting", queue.getQueue_callswaiting()));
//				queuescontent.append(analysisObject("queue-holdtime", queue.getQueue_holdtime()));
//				queuescontent.append(analysisObject("queue-seconds", queue.getQueue_seconds()));
//				queuescontent.append(analysisObject("queue-minutes", queue.getQueue_minutes()));
//				queuescontent.append(analysisObject("queue-periodic-announce", queue.getQueue_periodic_announce()));
//				queuescontent.append(analysisObject("queue-lessthan", queue.getQueue_lessthan()));
//				queuescontent.append(analysisObject("queue-reporthold", queue.getQueue_reporthold()));
//				queuescontent.append(analysisObject("reportholdtime", queue.getReportholdtime()));
//				queuescontent.append(analysisObject("retry", queue.getRetry()));
//				queuescontent.append(analysisObject("servicelevel", queue.getServicelevel()));
//				queuescontent.append(analysisObject("timeoutrestart", queue.getTimeoutrestart()));
//				queuescontent.append(analysisObject("weight", queue.getWeight()));
				queuescontent.append("\r\n\r\n\r\n");
			}
			String sipcontentStr = queuescontent.toString();
			bw.write(sipcontentStr.getBytes());
			isSuccess = true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage() +"修改Asterisk 队列配置文件出现异常！", e);
			isSuccess = false;
		}
		
		finally {
			try {
				bw.close();
				fos.close();
			} catch (IOException e) {
				logger.error(e.getMessage() +"修改Asterisk 队列配置文件关闭流时，出现异常！", e);
				e.printStackTrace();
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	/**
	 * 解析分机的各个字段，然后组合成需要的值后返回
	 * @param key		字段名称
	 * @param value		字段值
	 * @return String	组合结果
	 */
	private String analysisObject(String key, Object value) {
		if(key.equals("name")) {
			return "["+value+"]"+"\r\n";
		} else if(value == null) {
			return "";
		} else if(value.getClass() == String.class && "".equals(((String)value).trim())) {
			return "";
		} else if(value.equals(true)) {
			return key+"=yes"+"\r\n";
		} else if(value.equals(false)) {
			return key+"=no"+"\r\n";
		} else {
			return key+"="+value+"\r\n";
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Queue> loadPageEntities(int start, int length, String sql) {
		return queueEao.loadPageEntities(start, length, sql);
	}
	
	@Override
	public int getEntityCount(String sql) {
		return queueEao.getEntityCount(sql);
	}

	/** jrh 获取指定域下，所有可用的队列（非自动外呼队列、没有被其他项目使用的队列） */
	@Override
	public List<Queue> getAllUseableSimpleQueueByDomain(Domain domain) {
		return queueEao.getAllUseableSimpleQueueByDomain(domain);
	}


	//========  common method ========//


	@Override
	public Queue get(Object primaryKey) {
		return queueEao.get(Queue.class, primaryKey);
	}

	@Override
	public synchronized void save(Queue queue) {
		// 自动为新建的对列设置名称
		Long maxQueueName = queueEao.getMaxQueueName() + 1L;
		queue.setName(maxQueueName.toString());
		queueEao.save(queue);
	}

	@Override
	public synchronized Queue update(Queue queue) {
		if(queue.getId() == null) {		// 如果是新建的队列，则自动为其设置队列名
			Long maxQueueName = queueEao.getMaxQueueName() + 1L;
			queue.setName(maxQueueName.toString());
		}
		return queueEao.update(queue);
	}

	@Override
	public void delete(Queue queue) {
		queueEao.delete(queue);
	}

	@Override
	public void deleteById(Object primaryKey) {
		queueEao.delete(Queue.class, primaryKey);
	}

	//==========  getter setter  ==========//
	
	public QueueEao getQueueEao() {
		return queueEao;
	}

	public void setQueueEao(QueueEao queueEao) {
		this.queueEao = queueEao;
	}

	/**
	 * chb
	 * 通过队列名取得队列
	 * @param queueName
	 * @return
	 */
	@Override
	public Queue getQueueByQueueName(String queueName) {
		return queueEao.getQueueByQueueName(queueName);
	}

	@Override
	public List<Queue> getAllByJpql(String jpql) {
		return queueEao.getEntitiesByJpql(jpql);
	}

	@Override
	public boolean checkDeleteAbleByIvrAction(String queueName, Long domainId) {
		return queueEao.checkDeleteAbleByIvrAction(queueName, domainId);
	}

	@Override
	public Queue getByName(String queueName, Long domainId) {
		return queueEao.getByName(queueName, domainId);
	}
	
}
