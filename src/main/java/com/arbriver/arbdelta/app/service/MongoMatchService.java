package com.arbriver.arbdelta.app.service;

import com.arbriver.arbdelta.lib.model.ArbitrageBlock;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.converters.MatchMapper;
import com.arbriver.arbdelta.lib.model.dbmodel.MatchDTO;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MongoMatchService {
    private final MongoTemplate template;
    private final MatchMapper matchMapper;

    @Autowired
    public MongoMatchService(MongoTemplate mongoTemplate, MatchMapper matchMapper) {
        this.template = mongoTemplate;
        this.matchMapper = matchMapper;
    }

    public List<Match> listMatches() {
        return template.findAll(Match.class);
    }

    public ArbitrageBlock getArbitrageBlock(String matching_key) {
        return template.findById(matching_key, ArbitrageBlock.class);
    }

    public UpdateResult updateArbitrageBlock(ArbitrageBlock arbitrageBlock) {
        return template.replace(
                Query.query(Criteria.where("matching_key").is(arbitrageBlock.getMatching_key())),
                arbitrageBlock,
                ReplaceOptions.replaceOptions().upsert()
        );
    }

    //list all matches that have more than one book associated with them
    public List<Match> listCommonMatches() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("links").exists(true).not().size(1))
        );
        AggregationResults<MatchDTO> results = template.aggregate(aggregation, "matches", MatchDTO.class);
        List<MatchDTO> matchDTOS = results.getMappedResults();

        return matchDTOS.stream().map(matchMapper::fromDTO).toList();
    }

}
