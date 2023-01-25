import Link from "next/link";

const Index = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen py-2">
      <main className="flex flex-col items-center justify-center w-full flex-1 px-20 text-center">
        <h1 className="text-6xl font-bold">
          Welcome to{" "}
          <Link className="text-blue-600" href="https://nextjs.org">
            Next.js!
          </Link>
        </h1>
      </main>
    </div>
  );
}

export default Index;