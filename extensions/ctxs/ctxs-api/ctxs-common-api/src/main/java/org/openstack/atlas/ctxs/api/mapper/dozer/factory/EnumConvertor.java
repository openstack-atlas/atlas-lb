package org.openstack.atlas.ctxs.api.mapper.dozer.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dozer.CustomConverter;
import org.openstack.atlas.service.domain.exception.NoMappableConstantException;

import java.lang.reflect.Method;


public class EnumConvertor implements CustomConverter {
    public static Log LOG = LogFactory.getLog(EnumConvertor.class.getName());

    @Override
    public Object convert(Object existingDestinationFieldValue, Object sourceFieldValue, Class destinationClass, Class sourceClass) {

        LOG.debug(String.format("Convert: source value %s; source class %s, destination class %s" , ("" +sourceFieldValue), sourceClass.toString(), destinationClass.toString()));
        try
        {
            Method method = destinationClass.getMethod("values");
            Object[] values = (Object[]) method.invoke(null, new Object[0]);

            if (sourceFieldValue == null) {
                return null;
            }

            /*
            Method method = destinationClass.getMethod("values");
            Object[] values = (Object[]) method.invoke(null, new Object[0]);

            if (sourceFieldValue == null) {
                Method methodOrdinal = destinationClass.getMethod("ordinal");
                for (Object value : values)
                {
                    int ordinal = (Integer) methodOrdinal.invoke(value, new Object[0]);
                    if(ordinal == 0)
                        return value;
                }
                return null;
            }
            */
            String sourceFieldValueString = null;
            try
            {
                sourceFieldValueString = (String) sourceClass.getMethod("value").invoke(sourceFieldValue, new Object[0]);
            } catch (NoSuchMethodException nsme)
            {
                sourceFieldValueString = sourceFieldValue.toString();
            }
            Method destMethodValue = null;
            try {
                destMethodValue = destinationClass.getMethod("value");
            } catch (NoSuchMethodException nsme)
            {}
            for (Object value : values)
            {
                String destFieldValueString = null;
                if(destMethodValue != null)
                    destFieldValueString = (String)  destMethodValue.invoke(value, new Object[0]);
                else
                    destFieldValueString = value.toString();

                LOG.debug(String.format("Matching value %s with %s",sourceFieldValue.toString(),value.toString()));
                if(sourceFieldValueString.equals(destFieldValueString))
                {
                    return value;
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        throw new NoMappableConstantException("Cannot map source type: " + sourceClass.getName());
    }
}
