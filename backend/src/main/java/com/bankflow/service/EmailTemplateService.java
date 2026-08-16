package com.bankflow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final SpringTemplateEngine templateEngine;


    public String render(
            String templateName,
            Map<String, Object> variables
    ) {

        Context context = new Context();

        context.setVariables(variables);

        return templateEngine.process(
                "emails/" + templateName,
                context
        );
    }
}