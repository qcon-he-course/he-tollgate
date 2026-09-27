package com.halvern.tollgate.consultation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * One question in a query round, with the haulier's answer.
 */
@Data
@Entity
@Table(name = "query_question")
public class QueryQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int seq;
    private String questionText;
    private String answerText;
}
