package com.yamibuy.entity;

import java.io.Serializable;

import org.springframework.data.mybatis.annotations.Entity;
import org.springframework.data.mybatis.domains.LongId;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity(table = "ym_sendmail")
public class Sendmail extends LongId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer orderId;

	private String name;

	private String email;

	private String cc;

	private String subject;

	private Short count = Short.valueOf("0");

	private Integer send_time;

	private String content;
}
