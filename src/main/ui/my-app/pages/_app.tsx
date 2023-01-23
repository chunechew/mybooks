import Head from 'next/head';
import { AppProps } from 'next/app';
import { ReactQueryDevtools } from 'react-query/devtools';
import { QueryClient, QueryClientProvider } from 'react-query';
import Navbar from "../components/Navbar";
import "styles/styles.css";

const client = new QueryClient({
    defaultOptions: {
        queries: {
          refetchOnWindowFocus: false,
        },
    },
});

const App: React.FC<AppProps> = ({ Component, pageProps }) => {
  return (
    <QueryClientProvider client={client}>
        {process.env.NODE_ENV !== 'production' ? <ReactQueryDevtools initialIsOpen={false} /> : null /* 운영 모드가 아니면 화면 좌측 하단에 아이콘이 뜸 */}
        <Head>
            <title>MyBooks</title>
            {/* <meta name="viewport" content="width=device-width, initial-scale=1" /> */}
        </Head>
        <>
            <Navbar />
            <div className="container">
                <Component {...pageProps} />
            </div>
        </>
    </QueryClientProvider>
  );
}

export default App;