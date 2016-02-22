package ut.org.jfrog.bamboo;

import org.junit.Test;
import org.jfrog.bamboo.api.MyPluginComponent;
import org.jfrog.bamboo.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}