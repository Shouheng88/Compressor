package me.shouheng.compress.naming;

public final class DefaultNameFactory implements CacheNameFactory {

    public static DefaultNameFactory getFactory() {
        return new DefaultNameFactory();
    }

    private DefaultNameFactory() {
    }

    @Override
    public String getFileName() {
        return System.currentTimeMillis() + (int) (Math.random() * 1000) + ".jpg";
    }

}
