package com.gonsin.androidweb;

import android.os.Environment;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private static Logger logger = Logger.getLogger(ExampleInstrumentedTest.class.getSimpleName());

    @Test
    public void useAppContext() throws Exception {
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "office_files"
                + File.separator + "q.doc";

        File path = new File(Environment.getExternalStorageDirectory().getPath());
        String[] childs = path.list();
        for(String c : childs){
            logger.info(c);
        }

        File file = new File(filePath);

        logger.info(filePath);

        assertTrue(file.exists());
    }
}
