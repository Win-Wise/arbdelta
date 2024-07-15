package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
}
