/*
 * Copyright 2016, Google LLC All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.google.api.gax.rpc;

import com.google.api.core.ApiClock;
import com.google.api.core.ApiFunction;
import com.google.api.core.BetaApi;
import com.google.api.core.NanoClock;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.IOException;

/**
 * A base settings class to configure a service API class.
 *
 * <p>This base class includes settings that are applicable to all services, which includes things
 * like settings for creating an executor, credentials, transport-specific settings, and identifiers
 * for http headers.
 *
 * <p>If no ExecutorProvider is set, then InstantiatingExecutorProvider will be used, which creates
 * a default executor.
 */
public abstract class ClientSettings<SettingsT extends ClientSettings<SettingsT>> {

  private final ExecutorProvider executorProvider;
  private final CredentialsProvider credentialsProvider;
  private final HeaderProvider headerProvider;
  private final HeaderProvider internalHeaderProvider;
  private final TransportChannelProvider transportChannelProvider;
  private final ApiClock clock;
  private final String endpoint;

  /** Constructs an instance of ClientSettings. */
  protected ClientSettings(Builder builder) {
    this.executorProvider = builder.executorProvider;
    this.transportChannelProvider = builder.transportChannelProvider;
    this.credentialsProvider = builder.credentialsProvider;
    this.headerProvider = builder.headerProvider;
    this.internalHeaderProvider = builder.internalHeaderProvider;
    this.clock = builder.clock;
    this.endpoint = builder.endpoint;
  }

  public final ExecutorProvider getExecutorProvider() {
    return executorProvider;
  }

  public final TransportChannelProvider getTransportChannelProvider() {
    return transportChannelProvider;
  }

  public final CredentialsProvider getCredentialsProvider() {
    return credentialsProvider;
  }

  @BetaApi("The surface for customizing headers is not stable yet and may change in the future.")
  public final HeaderProvider getHeaderProvider() {
    return headerProvider;
  }

  @BetaApi("The surface for customizing headers is not stable yet and may change in the future.")
  protected final HeaderProvider getInternalHeaderProvider() {
    return internalHeaderProvider;
  }

  public final ApiClock getClock() {
    return clock;
  }

  public final String getEndpoint() {
    return endpoint;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("executorProvider", executorProvider)
        .add("transportChannelProvider", transportChannelProvider)
        .add("credentialsProvider", credentialsProvider)
        .add("headerProvider", headerProvider)
        .add("internalHeaderProvider", internalHeaderProvider)
        .add("clock", clock)
        .add("endpoint", endpoint)
        .toString();
  }

  public abstract <B extends Builder<SettingsT, B>> B toBuilder();

  public abstract static class Builder<
      SettingsT extends ClientSettings<SettingsT>, B extends Builder<SettingsT, B>> {

    private ExecutorProvider executorProvider;
    private CredentialsProvider credentialsProvider;
    private HeaderProvider headerProvider;
    private HeaderProvider internalHeaderProvider;
    private TransportChannelProvider transportChannelProvider;
    private ApiClock clock;
    private String endpoint;

    /** Create a builder from a ClientSettings object. */
    protected Builder(ClientSettings settings) {
      this.executorProvider = settings.executorProvider;
      this.transportChannelProvider = settings.transportChannelProvider;
      this.credentialsProvider = settings.credentialsProvider;
      this.headerProvider = settings.headerProvider;
      this.internalHeaderProvider = settings.internalHeaderProvider;
      this.clock = settings.clock;
      this.endpoint = settings.endpoint;
    }

    protected Builder(ClientContext clientContext) {
      if (clientContext == null) {
        this.executorProvider = InstantiatingExecutorProvider.newBuilder().build();
        this.transportChannelProvider = null;
        this.credentialsProvider = NoCredentialsProvider.create();
        this.headerProvider = new NoHeaderProvider();
        this.internalHeaderProvider = new NoHeaderProvider();
        this.clock = NanoClock.getDefaultClock();
        this.endpoint = null;
      } else {
        this.executorProvider = FixedExecutorProvider.create(clientContext.getExecutor());
        this.transportChannelProvider =
            FixedTransportChannelProvider.create(clientContext.getTransportChannel());
        this.credentialsProvider = FixedCredentialsProvider.create(clientContext.getCredentials());
        this.headerProvider = FixedHeaderProvider.create(clientContext.getHeaders());
        this.internalHeaderProvider =
            FixedHeaderProvider.create(clientContext.getInternalHeaders());
        this.clock = clientContext.getClock();
        this.endpoint = clientContext.getEndpoint();
      }
    }

    protected Builder() {
      this((ClientContext) null);
    }

    @SuppressWarnings("unchecked")
    protected B self() {
      return (B) this;
    }

    /**
     * Sets the ExecutorProvider to use for getting the executor to use for running asynchronous API
     * call logic (such as retries and long-running operations), and also to pass to the transport
     * settings if an executor is needed for the transport and it doesn't have its own executor
     * provider.
     */
    public B setExecutorProvider(ExecutorProvider executorProvider) {
      this.executorProvider = executorProvider;
      return self();
    }

    /** Sets the CredentialsProvider to use for getting the credentials to make calls with. */
    public B setCredentialsProvider(CredentialsProvider credentialsProvider) {
      this.credentialsProvider = Preconditions.checkNotNull(credentialsProvider);
      return self();
    }

    /**
     * Sets the HeaderProvider for getting custom static headers for http requests. The header
     * provider will be called during client construction only once. The headers returned by the
     * provider will be cached and supplied as is for each request issued by the constructed client.
     * Some reserved headers can be overridden (e.g. Content-Type) or merged with the default value
     * (e.g. User-Agent) by the underlying transport layer.
     */
    @BetaApi("The surface for customizing headers is not stable yet and may change in the future.")
    public B setHeaderProvider(HeaderProvider headerProvider) {
      this.headerProvider = headerProvider;
      return self();
    }

    /**
     * Sets the HeaderProvider for getting internal (library-defined) static headers for http
     * requests. The header provider will be called during client construction only once. The
     * headers returned by the provider will be cached and supplied as is for each request issued by
     * the constructed client. Some reserved headers can be overridden (e.g. Content-Type) or merged
     * with the default value (e.g. User-Agent) by the underlying transport layer.
     */
    @BetaApi("The surface for customizing headers is not stable yet and may change in the future.")
    protected B setInternalHeaderProvider(HeaderProvider internalHeaderProvider) {
      this.internalHeaderProvider = internalHeaderProvider;
      return self();
    }

    /**
     * Sets the TransportProvider to use for getting the transport-specific context to make calls
     * with.
     */
    public B setTransportChannelProvider(TransportChannelProvider transportChannelProvider) {
      this.transportChannelProvider = transportChannelProvider;
      return self();
    }

    /**
     * Sets the clock to use for retry logic.
     *
     * <p>This will default to a system clock if it is not set.
     */
    public B setClock(ApiClock clock) {
      this.clock = clock;
      return self();
    }

    public B setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return self();
    }

    /** Gets the ExecutorProvider that was previously set on this Builder. */
    public ExecutorProvider getExecutorProvider() {
      return executorProvider;
    }

    /** Gets the TransportProvider that was previously set on this Builder. */
    public TransportChannelProvider getTransportChannelProvider() {
      return transportChannelProvider;
    }

    /** Gets the CredentialsProvider that was previously set on this Builder. */
    public CredentialsProvider getCredentialsProvider() {
      return credentialsProvider;
    }

    /** Gets the custom HeaderProvider that was previously set on this Builder. */
    @BetaApi("The surface for customizing headers is not stable yet and may change in the future.")
    public HeaderProvider getHeaderProvider() {
      return headerProvider;
    }

    /** Gets the internal HeaderProvider that was previously set on this Builder. */
    @BetaApi("The surface for customizing headers is not stable yet and may change in the future.")
    protected HeaderProvider getInternalHeaderProvider() {
      return internalHeaderProvider;
    }

    /** Gets the ApiClock that was previously set on this Builder. */
    public ApiClock getClock() {
      return clock;
    }

    public String getEndpoint() {
      return endpoint;
    }

    /** Applies the given settings updater function to the given method settings builders. */
    protected static void applyToAllUnaryMethods(
        Iterable<UnaryCallSettings.Builder<?, ?>> methodSettingsBuilders,
        ApiFunction<UnaryCallSettings.Builder<?, ?>, Void> settingsUpdater)
        throws Exception {
      for (UnaryCallSettings.Builder<?, ?> settingsBuilder : methodSettingsBuilders) {
        settingsUpdater.apply(settingsBuilder);
      }
    }

    public abstract SettingsT build() throws IOException;

    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("executorProvider", executorProvider)
          .add("transportChannelProvider", transportChannelProvider)
          .add("credentialsProvider", credentialsProvider)
          .add("headerProvider", headerProvider)
          .add("internalHeaderProvider", internalHeaderProvider)
          .add("clock", clock)
          .add("endpoint", endpoint)
          .toString();
    }
  }
}
