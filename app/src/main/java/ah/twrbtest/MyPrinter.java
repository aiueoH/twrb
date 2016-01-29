package ah.twrbtest;

import com.twrb.core.MyLogger;

public class MyPrinter implements MyLogger.IPrinter {
    private boolean enable = true;
    private Level level = Level.V;

    private void systemOut(CharSequence s) {
        if (enable)
            System.out.println(s);
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public void v(CharSequence s) {
        if (level.equals(Level.V))
            systemOut(s);
    }

    @Override
    public void d(CharSequence s) {
        if (level.equals(Level.V) ||
            level.equals(Level.D))
            systemOut(s);
    }

    @Override
    public void i(CharSequence s) {
        if (level.equals(Level.V) ||
            level.equals(Level.D) ||
            level.equals(Level.I))
            systemOut(s);
    }

    @Override
    public void w(CharSequence s) {
        if (level.equals(Level.V) ||
            level.equals(Level.D) ||
            level.equals(Level.I) ||
            level.equals(Level.W))
            systemOut(s);
    }

    @Override
    public void a(CharSequence s) {
        if (level.equals(Level.V) ||
            level.equals(Level.D) ||
            level.equals(Level.I) ||
            level.equals(Level.W) ||
            level.equals(Level.A))
            systemOut(s);
    }

    @Override
    public void e(CharSequence s) {
        systemOut(s);
    }

    public enum Level {V, D, I, W, A, E}
}
