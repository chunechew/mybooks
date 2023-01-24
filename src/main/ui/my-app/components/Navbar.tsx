import Link from "next/link";
import { ReactElement, useEffect, useState } from "react";

export default function Navbar(): ReactElement {
    const [isNavExpanded, setIsNavExpanded] = useState(false);
    // const setLoggedInState = useSetClientState('loggedIn');

    // // React hook의 사용 위치 제한 우회용 코드
    // const useUpdate = (isLoggedIn: boolean) => {
    //     useEffect(() => {
    //         setLoggedInState(isLoggedIn);
    //     });

    //     return { loggedIn: isLoggedIn, setLoggedIn: setLoggedInState };
    // }

    return (
        <nav className="navigation">
            <Link href="/" className="brand-name">
                MyBooks
            </Link>
            <button 
                className="hamburger"
                onClick={() => {
                    setIsNavExpanded(!isNavExpanded);
                }}
            >
                {/* icon from heroicons.com */}
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-5 w-5"
                    viewBox="0 0 20 20"
                    fill="white"
                >
                    <path
                        fillRule="evenodd"
                        d="M3 5a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM3 10a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM9 15a1 1 0 011-1h6a1 1 0 110 2h-6a1 1 0 01-1-1z"
                        clipRule="evenodd"
                    />
                </svg>
            </button>
            <div className={
                isNavExpanded ? "navigation-menu expanded" : "navigation-menu"
            }>
                <ul>
                    <li>
                        <Link href="/">
                            Home
                        </Link>
                    </li>
                    <li>
                        <Link href="/login">
                            로그인
                        </Link>
                    </li>
                </ul>
            </div>
        </nav>
    );
}