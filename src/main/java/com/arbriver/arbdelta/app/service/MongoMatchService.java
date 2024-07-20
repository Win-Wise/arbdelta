package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.lib.model.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoMatchService {
    private final MongoTemplate template;

    @Autowired
    public MongoMatchService(MongoTemplate mongoTemplate) {
        this.template = mongoTemplate;
    }

    public List<Match> listMatches() {
        return template.findAll(Match.class);
    }

    //list all matches that have more than one book associated with them
    public List<Match> listCommonMatches() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("links").exists(true).not().size(1))
        );
        AggregationResults<Match> results = template.aggregate(aggregation, "matches", Match.class);
        return results.getMappedResults();
    }
}
