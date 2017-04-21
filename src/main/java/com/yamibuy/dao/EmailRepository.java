package com.yamibuy.dao;

import org.springframework.data.mybatis.repository.support.MybatisRepository;

import com.yamibuy.entity.Sendmail;

public interface EmailRepository extends MybatisRepository<Sendmail, Long> {

}
