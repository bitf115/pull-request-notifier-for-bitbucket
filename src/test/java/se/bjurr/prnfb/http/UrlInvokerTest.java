package se.bjurr.prnfb.http;

import static com.google.common.base.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.bjurr.prnfb.http.UrlInvoker.HTTP_METHOD.*;
import static se.bjurr.prnfb.listener.PrnfbPullRequestAction.APPROVED;
import static se.bjurr.prnfb.settings.PrnfbNotificationBuilder.prnfbNotificationBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import se.bjurr.prnfb.settings.PrnfbNotification;

public class UrlInvokerTest {

  private HttpRequestBase httpRequestBase;
  private UrlInvoker urlInvoker;

  @Before
  public void before() {
    this.urlInvoker =
        new UrlInvoker() {
          @Override
          HttpResponse doInvoke(
              final HttpRequestBase httpRequest, final HttpClientBuilder builder) {
            UrlInvokerTest.this.httpRequestBase = httpRequest;
            try {
              return new HttpResponse(new URI("http://fake.com"), 200, "");
            } catch (final URISyntaxException e) {
              e.printStackTrace();
              return null;
            }
          }
        } //
        .withUrlParam("http://url.com/");
  }

  @Test
  public void testThatHeadersAreAdded() {
    this.urlInvoker //
        .withHeader("name", "value") //
        .invoke();

    assertThat(this.httpRequestBase.getAllHeaders()) //
        .hasSize(1);
    assertThat(this.httpRequestBase.getAllHeaders()[0].getName()) //
        .isEqualTo("name");
    assertThat(this.httpRequestBase.getAllHeaders()[0].getValue()) //
        .isEqualTo("value");
  }

  @Test
  public void testThatHeadersAreAddedForBasicAuth() throws Exception {
    final PrnfbNotification notification =
        prnfbNotificationBuilder() //
            .withUrl("http://url.com/") //
            .withUser("user") //
            .withPassword("password") //
            .withTrigger(APPROVED) //
            .build();

    this.urlInvoker //
        .appendBasicAuth(notification) //
        .invoke();

    assertThat(this.httpRequestBase.getAllHeaders()) //
        .hasSize(1);
    assertThat(this.httpRequestBase.getAllHeaders()[0].getName()) //
        .isEqualTo("Authorization");
    assertThat(this.httpRequestBase.getAllHeaders()[0].getValue()) //
        .isEqualTo("Basic dXNlcjpwYXNzd29yZA==");
  }

  @Test
  public void testThatHttpEntityEnclosingRequestBaseCanBeCreatedAsPOSTWithoutContent() {
    final HttpRequestBase response =
        this.urlInvoker //
            .withMethod(POST) //
            .newHttpRequestBase();

    assertThat(response.getMethod()) //
        .isEqualTo(POST.name());
    assertThat(response) //
        .isInstanceOf(HttpRequestBase.class);
  }

  @Test
  public void testThatHttpEntityEnclosingRequestBaseCanBeCreatedAsPUTWithContent() {
    final HttpRequestBase response =
        this.urlInvoker //
            .withMethod(PUT) //
            .withPostContent(of("some content")) //
            .newHttpRequestBase();

    assertThat(response.getMethod()) //
        .isEqualTo(PUT.name());
    final HttpEntityEnclosingRequestBase c = (HttpEntityEnclosingRequestBase) response;
    assertThat(c.getEntity().getContentLength()) //
        .isGreaterThan(0);
  }

  @Test
  public void testThatHttpRequestBaseCanBeCreatedWithDelete() {
    final HttpRequestBase response =
        this.urlInvoker //
            .withMethod(DELETE) //
            .newHttpRequestBase();

    assertThat(response) //
        .isInstanceOf(HttpRequestBase.class);
    assertThat(response.getMethod()) //
        .isEqualTo(DELETE.name());
  }

  @Test
  public void testThatNoneSslCanBeConfigured() throws Exception {
    final HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);

    this.urlInvoker //
        .withUrlParam("http://url.com/") //
        .configureSsl(mockedBuilder);
  }

  @Test
  public void testThatProxyIsConfiguredIfThereIsAHostAndPort() throws Exception {
    final HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);

    this.urlInvoker //
        .withUrlParam("http://url.com/") //
        .withProxyServer(of("http://proxy.com/")) //
        .withProxyPort(123) //
        .configureProxy(mockedBuilder);
  }

  @Test
  public void testThatProxyIsNotConfiguredIfThereIsNoHost() throws Exception {
    final HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);

    this.urlInvoker //
        .withUrlParam("http://url.com/") //
        .withProxyPort(123) //
        .configureProxy(mockedBuilder);
  }

  @Test
  public void testThatProxyUserIsConfiguredIfItIsSet() throws Exception {
    final HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);

    this.urlInvoker //
        .withUrlParam("http://url.com/") //
        .withProxyServer(of("http://proxy.com/")) //
        .withProxyPort(123) //
        .withProxyUser(of("u")) //
        .withProxyPassword(of("p")) //
        .configureProxy(mockedBuilder);
  }

  @Test
  public void testThatProxyUserIsNotConfiguredIfNoPasswordSet() throws Exception {
    final HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);

    this.urlInvoker //
        .withUrlParam("http://url.com/") //
        .withProxyServer(of("http://proxy.com/")) //
        .withProxyPort(123) //
        .withProxyUser(of("u")) //
        .configureProxy(mockedBuilder);
  }

  @Test
  public void testThatSslCanBeConfigured() throws Exception {
    final HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
    final ClientKeyStore clientKeyStore = mock(ClientKeyStore.class);

    when(clientKeyStore.getKeyStore()).thenReturn(java.util.Optional.empty());

    this.urlInvoker //
        .withUrlParam("https://url.com/") //
        .withClientKeyStore(clientKeyStore) //
        .configureSsl(mockedBuilder);
  }

  @Test
  public void testThatURLCanBeInvokedWithMinimalConfig() throws Exception {
    this.urlInvoker //
        .invoke();

    assertThat(this.httpRequestBase.getMethod()) //
        .isEqualTo(GET.name());
    assertThat(this.httpRequestBase.getURI().toString()) //
        .isEqualTo(new URL("http://url.com/").toString());
  }
}
