import play.Environment;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This class configures filters that run on every request. This
 * class is queried by Play to get a list of filters.
 * <p>
 * Play will automatically use filters from any class called
 * <code>Filters</code> that is placed the root package. You can load filters
 * from a different class by adding a `play.http.filters` setting to
 * the <code>application.conf</code> configuration file.
 */
@Singleton
public class Filters implements HttpFilters {

    private final Environment env;

    /**
     * @param env Basic environment settings for the current application.
     */
    @Inject
    public Filters(Environment env) {
        this.env = env;
    }

    @Override
    public EssentialFilter[] filters() {
        return new EssentialFilter[]{};
    }

}
