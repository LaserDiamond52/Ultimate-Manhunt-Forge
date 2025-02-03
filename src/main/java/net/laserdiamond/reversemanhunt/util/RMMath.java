package net.laserdiamond.reversemanhunt.util;

import java.util.List;

public class RMMath {

    public static float getLeast(List<Float> values)
    {
        if (values.isEmpty())
        {
            return 0;
        }
        float ret = values.getFirst();
        for (float value : values)
        {
            if (value < ret)
            {
                ret = value;
            }
        }
        return ret;
    }

}
