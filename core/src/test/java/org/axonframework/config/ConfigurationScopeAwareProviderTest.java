package org.axonframework.config;

import org.axonframework.commandhandling.model.Repository;
import org.axonframework.eventhandling.saga.AnnotatedSagaManager;
import org.axonframework.messaging.ScopeAware;
import org.axonframework.messaging.ScopeDescriptor;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link ConfigurationScopeAwareProvider}.
 *
 * @author Rob van der Linden Vooren
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationScopeAwareProviderTest {

    @Mock
    private Configuration configuration;

    @Mock
    private AggregateConfiguration aggregateConfiguration;

    @Mock
    private Repository aggregateRepository;

    @Mock
    private SagaConfiguration sagaConfiguration;

    @Mock
    private AnnotatedSagaManager sagaManager;

    private ConfigurationScopeAwareProvider scopeAwareProvider;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        scopeAwareProvider = new ConfigurationScopeAwareProvider(configuration);
        when(configuration.findModules(any(Class.class))).thenAnswer(invocation -> {
            Class moduleType = invocation.getArgument(0, Class.class);
            return configuration.getModules().stream()
                .filter(m -> moduleType.isInstance(m.unwrap()))
                .map(m -> m.unwrap())
                .collect(Collectors.toList());
        });
    }

    @Test
    public void providesScopeAwareAggregatesFromModuleConfiguration() {
        when(configuration.getModules()).thenReturn(asList(new WrappingModuleConfiguration(aggregateConfiguration)));
        when(aggregateConfiguration.repository()).thenReturn(aggregateRepository);

        List<ScopeAware> components = scopeAwareProvider.provideScopeAwareStream(anyScopeDescriptor())
                                                        .collect(toList());

        assertThat(components, equalTo(asList(aggregateRepository)));
    }

    @Test
    public void providesScopeAwareSagasFromModuleConfiguration() {
        when(configuration.getModules()).thenReturn(asList(new WrappingModuleConfiguration(sagaConfiguration)));
        when(sagaConfiguration.getSagaManager()).thenReturn(sagaManager);

        List<ScopeAware> components = scopeAwareProvider.provideScopeAwareStream(anyScopeDescriptor())
                                                        .collect(toList());

        assertThat(components, equalTo(asList(sagaManager)));
    }

    @Test
    public void lazilyInitializes() {
        new ConfigurationScopeAwareProvider(configuration);

        verifyNoInteractions(configuration);
    }

    @Test
    public void cachesScopeAwareComponentsOnceProvisioned() {
        when(configuration.getModules()).thenReturn(asList(new WrappingModuleConfiguration(aggregateConfiguration)));
        when(aggregateConfiguration.repository()).thenReturn(aggregateRepository);

        // provision once
        List<ScopeAware> first = scopeAwareProvider.provideScopeAwareStream(anyScopeDescriptor())
                                                   .collect(toList());
        reset(configuration, aggregateConfiguration);

        // provision twice
        List<ScopeAware> second = scopeAwareProvider.provideScopeAwareStream(anyScopeDescriptor()).collect(toList());
        verifyNoInteractions(configuration);
        verifyNoInteractions(aggregateConfiguration);
        assertThat(second, equalTo(first));
    }

    private static ScopeDescriptor anyScopeDescriptor() {
        return (ScopeDescriptor) () -> "test-scope";
    }

    /**
     * Test variant of a {@link #unwrap() wrapping} configuration.
     */
    private static class WrappingModuleConfiguration implements ModuleConfiguration {

        private final ModuleConfiguration delegate;

        WrappingModuleConfiguration(ModuleConfiguration delegate) {
            this.delegate = delegate;
        }

        @Override
        public void initialize(Configuration config) {
            // No-op, only ipmlemented for test case
        }

        @Override
        public void start() {
            // No-op, only ipmlemented for test case
        }

        @Override
        public void shutdown() {
            // No-op, only ipmlemented for test case
        }

        @Override
        public ModuleConfiguration unwrap() {
            return delegate == null ? this : delegate;
        }
    }
}
