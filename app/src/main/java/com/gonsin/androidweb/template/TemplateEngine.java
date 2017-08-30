package com.gonsin.androidweb.template;

import net.asfun.jangod.base.Application;
import net.asfun.jangod.base.Configuration;
import net.asfun.jangod.cache.ConcurrentHashPool;
import net.asfun.jangod.cache.StatefulObjectPool;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by monday on 2017/8/30.
 */
public class TemplateEngine {

    StatefulObjectPool<Processor> pool;
    Application application;

    public TemplateEngine() {
        application = new Application();
        initProcessorPool();
    }

    public TemplateEngine(Application application) {
        this.application = application;
        initProcessorPool();
    }

    /*
     * Using ConcurrentHashPool instead of ConcurrentListPool.
     * Caused by: ConcurrentSkipListMap is added in API level 9.
     * Modified by: join.
     */
    @SuppressWarnings("unchecked")
    protected void initProcessorPool() {
        String poolClass = application.getConfiguration().getProperty("processor.pool");
        if (poolClass == null) {
            pool = new ConcurrentHashPool<Processor>();
        } else {
            try {
                pool = (StatefulObjectPool<Processor>) Class.forName(poolClass).newInstance();
            } catch (Exception e) {
                pool = new ConcurrentHashPool<Processor>();
//                logger.warning("Can't instance processor pool(use default) >>> " + poolClass);
            }
        }
    }

    public void setEngineBindings(Map<String, Object> bindings) {
        if (bindings == null) {
            application.getGlobalBindings().clear();
        } else {
            application.setGlobalBindings(bindings);
        }
    }

    /**
     * 模板文件生成指定字符串
     * @param templateFile
     * @param bindings
     * @return
     * @throws IOException
     */
    public String process(String templateFile, Map<String, Object> bindings) throws IOException {
        Processor processor = pool.pop();
        if (processor == null) {
            processor = new Processor(application);
        }
        String result = processor.render(templateFile, bindings);
        pool.push(processor);
        return result;
    }

    public void process(String templateFile, Map<String, Object> bindings, Writer out)
            throws IOException {
        Processor processor = pool.pop();
        if (processor == null) {
            processor = new Processor(application);
        }
        processor.render(templateFile, bindings, out);
        pool.push(processor);
    }

    public Configuration getConfiguration() {
        return application.getConfiguration();
    }

}
