import styled from "styled-components";
import Navbar from "./Navbar";

type Props = {
  children: React.ReactNode;
};

export default function Layout(props: Props) {
  const MainWrapper = styled.div`
    height: calc(100vh - 64px);
  `;
  
  return (
    <div className="w-full p-0">
      <Navbar />
      <MainWrapper>
        {props.children}
      </MainWrapper>
    </div>
  );
}