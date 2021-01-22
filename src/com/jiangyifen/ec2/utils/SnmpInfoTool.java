package com.jiangyifen.ec2.utils;

import java.io.IOException;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.jiangyifen.ec2.ui.mgr.system.realtime.pojo.form.SystemStatistic;

public class SnmpInfoTool {

	private static Snmp snmp;
	private static CommunityTarget target;
	static {
		try {
			String ip = Config.props.getProperty(Config.SNMP_IP);
			// 设置管理进程的IP和端口
			Address targetAddress = GenericAddress.parse("udp:"+ip+"/161");
			@SuppressWarnings("rawtypes")
			TransportMapping transport;
			transport = new DefaultUdpTransportMapping();
			// 设定采取的协议
			snmp = new Snmp(transport);
			//USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0); SecurityModels.getInstance().addSecurityModel(usm);
		    transport.listen();
			target = new CommunityTarget();	 //设置 target
			target.setCommunity(new OctetString("public"));
			target.setAddress(targetAddress);
			target.setRetries(2);// 通信不成功时的重试次数
			target.setTimeout(5000); // 超时时间
			target.setVersion(SnmpConstants.version2c);//  // snmp版本
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({"unchecked"})
	public static SystemStatistic findSystemStatistic(){
		SystemStatistic statistic = new SystemStatistic();
		try {
			PDU request = new PDU();
			request.setType(PDU.GET);//GET);//数据获取方式为GET
			//request.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.1.0")));//系统信息
			request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.4.3.0")));// 交换总容量
			request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.4.4.0")));// 交换可用容量
			request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.4.5.0")));// 内存总容量
			request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.4.6.0")));// 内存使用容量
			
			//request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.9.1.6.1")));//硬盘/总量
			//request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.9.1.7.1")));//硬盘/可用
			//request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.9.1.8.1")));//硬盘/已用
			request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.9.1.9.1")));//硬盘使用率
			request.add(new VariableBinding(new OID(".1.3.6.1.4.1.2021.11.11.0")));//CPU空闲数
			request.add(new VariableBinding(new OID(".1.3.6.1.2.1.25.1.1.0")));//	系统运行
			ResponseEvent respEvnt = snmp.send(request, target);//向Agent发送PDU，并接收Response
			if (respEvnt != null && respEvnt.getResponse() != null) {
				PDU response = respEvnt.getResponse();
				int errorStatus = response.getErrorStatus();
				if (errorStatus == PDU.noError) {
					Vector<VariableBinding> recVBs = (Vector<VariableBinding>) respEvnt.getResponse().getVariableBindings();
					for (int i = 0; i < recVBs.size(); i++) {
						VariableBinding recVB = recVBs.elementAt(i);
						String oid = recVB.getOid().toString();
							  if("1.3.6.1.4.1.2021.4.3.0".equals(oid)){					//交换容量
							statistic.setMemorySwapTotal(recVB.getVariable().toLong());
						}else if("1.3.6.1.4.1.2021.4.4.0".equals(oid)){				//交换可用
							statistic.setMemorySwapFree(recVB.getVariable().toLong());
						}else if("1.3.6.1.4.1.2021.4.5.0".equals(oid)){				//内存容量
							statistic.setMemoryRamTotal(recVB.getVariable().toLong());	
						}else if("1.3.6.1.4.1.2021.4.6.0".equals(oid)){				//内存可用
							statistic.setMemoryRamFree(recVB.getVariable().toLong());
						}else if("1.3.6.1.4.1.2021.9.1.9.1".equals(oid)){			//硬盘使用率
							statistic.setDiskRootUse(recVB.getVariable().toLong());
 						}else if("1.3.6.1.4.1.2021.11.11.0".equals(oid)){			//CPU空闲率
							statistic.setCupUse(recVB.getVariable().toLong());
						}else if("1.3.6.1.2.1.25.1.1.0".equals(oid)){			//系统运行时间
							statistic.setRunTime(recVB.getVariable().toString());
						}
						statistic.setFindCode(1);
					}
				} else {
					statistic.setFindCode(0);
					statistic.setFindMsg("获得SNMP连接失败");
				}
			}else{
				statistic.setFindCode(0);
				statistic.setFindMsg("获得SNMP连接失败");
			}
		} catch (Exception e) {
			statistic.setFindCode(-1);
			statistic.setFindMsg("获得SNMP数据异常"); 
		}
		return statistic;
	}
	
	public static Long findLongVal(String val){
		long lv = 0;
		
		
		return lv;
	}
}


/*int errorIndex = response.getErrorIndex();
String errorStatusText = response.getErrorStatusText();*/