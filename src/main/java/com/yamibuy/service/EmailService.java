package com.yamibuy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yamibuy.dao.EmailRepository;
import com.yamibuy.entity.Sendmail;

@Service
public class EmailService {

	@Autowired
	private EmailRepository emailRepository;

	public Page<Sendmail> list(int page, int size) {
		PageRequest pageable = new PageRequest(page, size);
		return emailRepository.findAll(pageable);
	}

}
