import Link from "next/link";

type Props = {
    href: string;
    children?: React.ReactNode;
    menuClose?: Function;
    onClick?: Function;
    className?: string;
};

const MobileMenuLink = (props: Props) => {
    const parentFunc = props.menuClose;

    const menuClose = () => {
        if(props.menuClose) {
            props.menuClose(false);
        }
    }

    const onClick = () => {
        if(props.onClick) {
            props.onClick();
        }
        menuClose();
    }

    return (
        <Link className={props.className} href={props.href} onClick={() => {onClick();}}>
            {props.children}
        </Link>
    );
}

export default MobileMenuLink;