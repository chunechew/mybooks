import { AppProps } from 'next/app';
import Head from 'next/head';
import { Hydrate, QueryClient, QueryClientProvider } from 'react-query';
import { ReactQueryDevtools } from 'react-query/devtools';
import "../styles/globals.css";
import Layout from "../components/Layout";
import { SessionProvider } from 'next-auth/react';

const client = new QueryClient({
    defaultOptions: {
        queries: {
          refetchOnWindowFocus: false,
        },
    },
});

const App: React.FC<AppProps> = ({ Component, pageProps }) => {
  return (
    <SessionProvider session={pageProps.session}>
      <QueryClientProvider client={client}>
        <Hydrate state={pageProps.dehydratedState}>
          {process.env.NODE_ENV !== 'production' ? <ReactQueryDevtools initialIsOpen={false} /> : null /* 운영 모드가 아니면 화면 좌측 하단에 아이콘이 뜸 */}
          <Head>
              <title>MyBooks</title>
              {/* <meta name="viewport" content="width=device-width, initial-scale=1" /> */}
          </Head>
          <Layout>
            <Component {...pageProps} />
          </Layout>
        </Hydrate>
      </QueryClientProvider>
    </SessionProvider>
  );
}

export default App;