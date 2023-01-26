import Navbar from "./Navbar";

type Props = {
  children: React.ReactNode;
};
export default function Layout(props: Props) {
  return (
    <>
      <Navbar />
      <div className="mainWrapper">
        {props.children}
      </div>
    </>
  );
}