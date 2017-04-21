package com.yamibuy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yamibuy.central.core.entity.BaseResponse;
import com.yamibuy.entity.Sendmail;
import com.yamibuy.service.EmailService;

@RestController
public class EmailController {

	@Autowired
	private EmailService emailService;

	@GetMapping("list")
	public Page<Sendmail> list(@RequestParam(value = "page", required = false) Integer page,
	        @RequestParam(value = "size", required = false) Integer size) {
		int defaultPage = null != page ? page : 0;
		int defaultSize = null != size ? size : 20;
		return emailService.findAll(defaultPage, defaultSize);
	}

	@GetMapping("send")
	public BaseResponse<String> send() {
		return emailService.send();
	}
}
