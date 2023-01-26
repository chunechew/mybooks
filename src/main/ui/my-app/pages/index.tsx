import Link from "next/link";
import tw from "tailwind-styled-components";

const Index = () => {
  const IndexWrapper = tw.div`
    flex
    flex-col
    items-center
    justify-center
    py-2
  `;

  return (
    <IndexWrapper className="mainWrapper">
      <main className="flex flex-col items-center justify-center w-full flex-1 px-20 text-center">
        <h1 className="text-6xl font-bold">
          Welcome to{" "}
          <Link className="text-blue-600" href="/">
            MyBooks!
          </Link>
        </h1>
      </main>
    </IndexWrapper>
  );
}

export default Index;