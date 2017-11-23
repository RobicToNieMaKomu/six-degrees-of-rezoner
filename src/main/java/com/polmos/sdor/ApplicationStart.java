package com.polmos.sdor;

import com.polmos.sdor.github.crawlers.CrawlersManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

/**
 * Created by RobicToNieMaKomu on 2017-10-26.
 */
@Service
public class ApplicationStart implements ApplicationListener<ContextRefreshedEvent> {
    private final CrawlersManager manager;
    private final int crawlerThreads;

    public ApplicationStart(CrawlersManager manager,
                            @Value("${crawler.threads}") int crawlerThreads) {
        this.manager = manager;
        this.crawlerThreads = crawlerThreads;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        IntStream
                .range(0, crawlerThreads)
                .forEach(i -> manager.launchCrawler());
    }
}
