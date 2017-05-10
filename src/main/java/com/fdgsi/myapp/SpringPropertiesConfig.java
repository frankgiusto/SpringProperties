package ca.gc.ic.cipo.tm.madridconsole.web;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.configuration.DatabaseConfiguration;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.InitializingBean;import org.springframework.context.EnvironmentAware;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.context.annotation.PropertySource;import org.springframework.context.annotation.PropertySources;import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;import org.springframework.core.env.ConfigurableEnvironment;import org.springframework.core.env.Environment;import org.springframework.core.env.MutablePropertySources;import org.springframework.core.env.PropertiesPropertySource;import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;import org.springmodules.commons.configuration.CommonsConfigurationFactoryBean;
/** * Spring 4 Properties Java Configuration, with a Database Properties table to store most values and a small * application.properties file too. The Database table will take precedence over the properties file with this setup */@Configuration@PropertySources({    @PropertySource(value = "classpath:application-configuration.properties", ignoreResourceNotFound = false),    @PropertySource("classpath:configuration.properties")})
public class SpringPropertiesConfig extends PropertySourcesPlaceholderConfigurer    implements EnvironmentAware, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(SpringPropertiesConfig.class);
    private Environment environment;
    @Override    public void setEnvironment(Environment environment) {        // save off Environment for later use        this.environment = environment;        super.setEnvironment(environment);    }
    @Override    public void afterPropertiesSet() throws Exception {
        MutablePropertySources envPropSources = ((ConfigurableEnvironment) environment).getPropertySources();
        try {
            // dataSource, Table Name, Key Column, Value Column            DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(mydataSource(),                environment.getProperty("config.table.name"), environment.getProperty("config.table.key"),                environment.getProperty("config.table.value"));
            CommonsConfigurationFactoryBean commonsConfigurationFactoryBean = new CommonsConfigurationFactoryBean(                databaseConfiguration);
            Properties dbProps = (Properties) commonsConfigurationFactoryBean.getObject();            PropertiesPropertySource dbPropertySource = new PropertiesPropertySource("dbPropertySource", dbProps);
            // By being First, Database Properties take precedence over all other properties that have the same key name            // You could put this last, or just in front of the application.properties if you wanted to...            envPropSources.addFirst(dbPropertySource);        } catch (Exception e) {            log.error("Error during database properties setup", e);            throw new RuntimeException(e);        }
    }
    @Bean    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {        return new PropertySourcesPlaceholderConfigurer();    }
    @Bean    public DataSource mydataSource() {        final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();        dsLookup.setResourceRef(true);        DataSource dataSource = dsLookup.getDataSource(environment.getProperty("config.jndiName"));        return dataSource;    }}
