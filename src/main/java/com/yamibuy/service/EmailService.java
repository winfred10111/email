package com.yamibuy.service;

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

	@Scheduled(cron = "0 0/5 * * * ?")
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
			String mailTo = getMailTo(sendmail.getEmail());
			if (StringUtils.isEmpty(mailTo)) {
				log.info("mail to user not in white list, {}", sendmail.getEmail());
				return;
			}
			helper.setTo(mailTo);
			helper.setSubject(sendmail.getSubject());
			// String cc = sendmail.getCc();
			// if (null != cc) {
			// String[] ccArray = cc.split(",");
			// for (String ccArr : ccArray) {
			// getMailTo(ccArr);
			// }
			// helper.setCc(cc);
			// }
			helper.setText(sendmail.getContent(), true);
			javaMailSender.send(mimeMessage);
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

	protected String getMailTo(String email) {
		// String defaultEmail = "charles.kou@yamibuy.com";
		// 设置收件人，如在白名单中则直接使用
		if (StringUtils.isNotEmpty(whiteList) && !"${MAIL_WHITE_LIST}".equalsIgnoreCase(whiteList)
				&& whiteList.contains(email.trim())) {
			return email;
		}
		// 否则发送到默认收件人
		// log.info("替换收件人 {} 为默认收件人 {}", email, defaultEmail);
		return "";
	}

}
