package cn.v5.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

/**
 * Created by fangliang on 16/5/26.
 */
@Service
public class MessageSourceService {

    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;

    @Autowired
    @Qualifier("messageSource1")
    private MessageSource messageSource1;


    public MessageSource getMessageSource(Integer appID) {

        if (appID != null) {

            switch (appID) {
                case 0:
                    return messageSource;
                case 1:
                    return messageSource1;
            }
        }
        return messageSource;

    }


}
