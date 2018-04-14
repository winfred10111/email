package com.yamibuy.service;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mybatis.repository.util.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.yamibuy.central.core.entity.BaseResponse;
import com.yamibuy.central.core.util.FixedThreadPoolUtil;
import com.yamibuy.dao.EmailRepository;
import com.yamibuy.entity.Sendmail;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

	@Autowired
	protected EmailRepository emailRepository;

	@Autowired
	protected JavaMailSender javaMailSender;

	@Value("${MAIL_WHITE_LIST}")
	private String whiteList;

	public Page<Sendmail> findAll(int page, int size) {
		Sort sort = new Sort(Direction.DESC, "id");
		PageRequest pageable = new PageRequest(page, size, sort);
		return emailRepository.findAll(pageable);
	}

	public List<Sendmail> findAll() {
		return emailRepository.findAll();
	}

	@Scheduled(cron = "0 0/1 * * * ?")
	public void scheduleTask() throws InterruptedException {
		send();
	}

	public BaseResponse<String> send() {
		log.info("email send task scheduled");
		List<Sendmail> list = findAll();
		for (Sendmail sendmail : list) {
			Short count = sendmail.getCount();
			if (null != count && count < 4) {
				FixedThreadPoolUtil.doExecutor(() -> send(sendmail));
			}
		}

		return BaseResponse.SUCCESS;
	}

	private void send(Sendmail sendmail) {
		Long id = sendmail.getId();
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			//
			List<String> mailTo = getMailTo(sendmail.getEmail());
			if (mailTo.isEmpty()) {
				log.info("mail to user not in white list, {}", sendmail.getEmail());
				return;
			}
			String[] emailArray = new String[mailTo.size()];
			emailArray = mailTo.toArray(emailArray);
			helper.setTo(emailArray);
			helper.setSubject(sendmail.getSubject());
			helper.setText(sendmail.getContent(), true);
//			javaMailSender.send(mimeMessage);
			log.info("email send success, id = {}, mailTo", id, mailTo);
			sendmail.setCount(Short.MAX_VALUE);
			emailRepository.update(sendmail);
			log.info("email record count updated, id = {}", id);
		} catch (Exception e) {
			log.error("email send failed, id = {}", id);
			log.error(e.getLocalizedMessage(), e);
			Short defaultCount = 1;
			Short oldCount = sendmail.getCount();
			Short newCount = null != oldCount ? Integer.valueOf(oldCount + 1).shortValue() : defaultCount;
			sendmail.setCount(newCount);
			emailRepository.update(sendmail);
			log.error("email record count update, count = {}", newCount);
		}
	}

	public List<String> getMailTo(String email) {
		// 设置收件人，如在白名单中则直接使用
		List<String> emailList = new ArrayList<>();
		String[] split = new String[1];
		if (null != email && email.length() > 0) {
			if (email.contains(",")) {
				split = email.split(",");
			} else if (email.contains(";")) {
				split = email.split(";");
			} else {
				split[0] = email;
			}

			for (String to : split) {
				if (StringUtils.isNotEmpty(whiteList) && !"${MAIL_WHITE_LIST}".equalsIgnoreCase(whiteList)
						&& whiteList.contains(to.trim())) {
					emailList.add(to);
				}
			}
		}

		return emailList;
	}
}
