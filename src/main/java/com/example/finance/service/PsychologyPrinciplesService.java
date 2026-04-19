package com.example.finance.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PsychologyPrinciplesService {

    public List<String> principles() {
        return List.of("Behavior beats spreadsheets over long horizons.",
                "Preserve room for error before maximizing returns.",
                "Compounding rewards consistency more than intensity.",
                "Build plans that are realistic for your personality.",
                "Avoid short-term emotional decisions driven by greed or fear.");
    }

    public List<String> strictMentorToneRules() {
        return List.of("Use concise, direct language with no fluff.",
                "Call out risky or impulsive behavior explicitly.", "Prioritize disciplined and repeatable habits.",
                "Finish with one concrete next step the user can execute immediately.");
    }
}
