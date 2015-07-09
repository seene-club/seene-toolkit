package org.vanitasvitae.depthmapneedle;

/**
 * Created by vanitas on 28.04.15.
 * https://github.com/vanitasvitae/DepthMapNeedle
 */
public abstract class Base64Wrapper
{
    public abstract byte[] decode(byte[] data);
    public abstract byte[] encode(byte[] data);
}
