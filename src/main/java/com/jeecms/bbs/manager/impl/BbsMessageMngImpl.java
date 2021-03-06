package com.jeecms.bbs.manager.impl;


import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;





import com.jeecms.bbs.dao.BbsMessageDao;
import com.jeecms.bbs.entity.BbsMessage;
import com.jeecms.bbs.entity.BbsMessageReply;
import com.jeecms.bbs.entity.BbsUser;
import com.jeecms.bbs.manager.BbsMessageMng;
import com.jeecms.bbs.manager.BbsMessageReplyMng;
import com.jeecms.bbs.manager.BbsUserMng;
import com.jeecms.common.hibernate3.Updater;
import com.jeecms.common.page.Pagination;

@Service
@Transactional
public class BbsMessageMngImpl implements BbsMessageMng {
	@Transactional(readOnly = true)
	public BbsMessage findById(Integer id) {
		BbsMessage entity = dao.findById(id);
		return entity;
	}

	public BbsMessage save(BbsMessage bean) {
		dao.save(bean);
		return bean;
	}

	public BbsMessage update(BbsMessage bean) {
		Updater<BbsMessage> updater = new Updater<BbsMessage>(bean);
		bean = dao.updateByUpdater(updater);
		return bean;
	}

	public BbsMessage deleteById(Integer id) {
		BbsMessage bean = dao.deleteById(id);
		return bean;
	}

	public BbsMessage[] deleteByIds(Integer[] ids) {
		BbsMessage[] beans = new BbsMessage[ids.length];
		for (int i = 0, len = ids.length; i < len; i++) {
			beans[i] = deleteById(ids[i]);
		}
		return beans;
	}

	public BbsMessage getMsg(String username,String sendername,String receivername,Integer typeId){
		return dao.getSendRelation(manager.findByUsername(username).getId(), manager.findByUsername(sendername).getId(),manager.findByUsername(receivername).getId(),typeId);
	}
	
	private BbsMessage getSendRelation(Integer userId, Integer senderId, Integer receiverId,Integer typeId) {
		return dao.getSendRelation(userId, senderId, receiverId,typeId);
	}

	public void sendMsg(BbsUser sender, BbsUser receiver, BbsMessage sMsg) {
		Integer senderId = sender.getId();
		Integer receiverId = receiver.getId();
		Integer typeId=sMsg.getMsgType();
		//处理发送端
		BbsMessage msg = getSendRelation(senderId, senderId, receiverId,typeId);
		BbsMessage rMsg = sMsg.putDataAndClone(sender, receiver);
		saveOrUpdate(msg, sMsg);
		//处理接收端
		msg = getSendRelation(receiverId, senderId, receiverId,typeId);
		saveOrUpdates(msg, rMsg);
	}
	
	public void sendSysMsg(BbsUser sender, BbsMessage sMsg){
		sMsg.setCreateTime(Calendar.getInstance().getTime());
		sMsg.setMsgType(BbsMessage.MESSAGE_TYPE_MESSAGE);
		sMsg.setSys(true);
		sMsg.setStatus(false);
		sMsg.setSender(sender);
		sMsg.setUser(sender);
		save(sMsg);
	}
	
	public Pagination getPageByUserId(Integer userId, Integer typeId,Integer pageNo, Integer pageSize) {
		return dao.getPageByUserId(userId,typeId, pageNo, pageSize);
	}
	
	public boolean hasUnReadMessage(Integer userId, Integer typeId) {
		if(dao.getListByUserIdStatus(userId, typeId, false).size()>0){
			return true;
		}
		return false;
	}
	public List<BbsMessage> getListUserIdStatus(Integer userId, Integer typeId,
			Boolean status) {
		return dao.getListByUserIdStatus(userId, typeId, status);
	}
	
	public List<BbsMessage> getListByUsername(String username){
		return dao.getListByUsername(username);
	}
	
	public Pagination getPagination(Boolean sys,Integer pageNo,Integer pageSize){
		return dao.getPagination(sys,pageNo, pageSize);
	}
	
	private void saveOrUpdate(BbsMessage msg, BbsMessage bean){
		//无论是更新还是新的留言或者回复都需要设置未读
		bean.setStatus(false);
		if (msg == null) {
			save(bean);
		}else{
			bean.setId(msg.getId());
			update(bean);
		}
		BbsMessageReply reply = bean.createReply();
		reply.setStatus(true);
		reply.setIsnotification(true);
		replyMng.save(reply);
	}
	
	
	private void saveOrUpdates(BbsMessage msg, BbsMessage bean){
		//无论是更新还是新的留言或者回复都需要设置未读
		bean.setStatus(false);
		if (msg == null) {
			save(bean);
		}else{
			bean.setId(msg.getId());
			update(bean);
		}
		BbsMessageReply reply = bean.createReply();
		reply.setStatus(false);
		reply.setIsnotification(false);
		replyMng.save(reply);
	}
	
	
	
	

	private BbsMessageDao dao;
	private BbsMessageReplyMng replyMng;

	@Autowired
	private BbsUserMng manager;
	@Autowired
	public void setDao(BbsMessageDao dao) {
		this.dao = dao;
	}
	@Autowired
	public void setReplyMng(BbsMessageReplyMng replyMng) {
		this.replyMng = replyMng;
	}
	
}