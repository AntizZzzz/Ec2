package com.jiangyifen.ec2.service.csr.ami.impl;

import org.asteriskjava.manager.action.QueueAddAction;
import org.asteriskjava.manager.action.QueueRemoveAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;

public class QueueMemberRelationServiceImpl implements QueueMemberRelationService {

	@Override
	public void addQueueMemberRelation(String queueName, String exten, Integer priority) {
		QueueAddAction queueAddAction = new QueueAddAction(queueName, "SIP/" + exten, priority);
		AmiManagerThread.sendAction(queueAddAction);
	}

	@Override
	public void removeQueueMemberRelation(String queueName, String exten) {
		QueueRemoveAction queueRemoveAction = new QueueRemoveAction(queueName, "SIP/" + exten);
		AmiManagerThread.sendAction(queueRemoveAction);
	}

}
