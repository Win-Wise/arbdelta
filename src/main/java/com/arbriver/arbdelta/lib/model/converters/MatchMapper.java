package com.arbriver.arbdelta.lib.model.converters;

import com.arbriver.arbdelta.lib.model.Fixture;
import com.arbriver.arbdelta.lib.model.Match;
import com.arbriver.arbdelta.lib.model.constants.Bookmaker;
import com.arbriver.arbdelta.lib.model.dbmodel.FixtureDTO;
import com.arbriver.arbdelta.lib.model.dbmodel.MatchDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class MatchMapper {

    public FixtureDTO toFixtureDTO(Fixture fixture) {
        return new FixtureDTO(
                fixture.getEventID(),
                fixture.getScore(),
                fixture.getText(),
                fixture.getStartTime(),
                fixture.getBook(),
                fixture.getHome(),
                fixture.getAway()
        );
    }

    public Match fromDTO(MatchDTO dto) {
        Match.MatchBuilder m = Match.builder()
                .id(dto.get_id())
                .home(dto.getHome())
                .away(dto.getAway())
                .sport(dto.getSport())
                .startTime(dto.getStart_time())
                .text(dto.getText());

        HashMap<Bookmaker, Fixture> map = new HashMap<>();
        for(FixtureDTO fixtureDTO: dto.getLinks()) {
            Fixture.FixtureBuilder fb = Fixture.builder()
                    .startTime(fixtureDTO.getStart_time())
                    .eventID(fixtureDTO.getEvent_id())
                    .score(fixtureDTO.getScore())
                    .text(fixtureDTO.getText())
                    .away(fixtureDTO.getAway())
                    .home(fixtureDTO.getHome())
                    .book(fixtureDTO.getBook())
                    .hyperlink(fixtureDTO.getHyperlink());
            map.put(fixtureDTO.getBook(), fb.build());
        }

        m.links(map);
        return m.build();
    }
}
