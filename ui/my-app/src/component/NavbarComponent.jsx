import Container from 'react-bootstrap/Container'
import Nav from 'react-bootstrap/Nav'
import Navbar from 'react-bootstrap/Navbar'
import {useEffect, useState} from "react";
import AuthService from "../service/AuthService";

const NavbarComponent = () => {

    const [showInsurerBoard, setShowInsurerBoard] = useState(false)
    const [showInsureeBoard, setShowInsureeBoard] = useState(false)
    const [currentUser, setCurrentUser] = useState(undefined)

    useEffect(() => {
        const user = AuthService.getCurrentUser()
        if (user) {
            setCurrentUser(user)
            setShowInsurerBoard(user.roles.includes("ROLE_INSURER"))
            setShowInsureeBoard(user.roles.includes("ROLE_INSUREE"))
        }
    }, [])

    const logOut = () => {
        AuthService.logout()
    }

    return (
        <Navbar bg="light" variant="light">
            <Container>
                <Nav className="me-auto">
                    <Nav.Link href="/">Home</Nav.Link>

                    {showInsurerBoard && (
                        <>
                            <Nav.Link href="/peers">Peers</Nav.Link>
                            <Nav.Link href="/insurance">Insurance</Nav.Link>
                            <Nav.Link href="/insurances">Insurances</Nav.Link>
                        </>
                    )}

                    {showInsureeBoard && (
                        <>
                            <Nav.Link href="/peers">Peers</Nav.Link>
                            <Nav.Link href="/insurances">Insurances</Nav.Link>
                        </>
                    )}

                    {currentUser ? (
                        <>
                            <Nav.Link href="/profile">Profile</Nav.Link>
                            <Nav.Link href="/login" onClick={logOut}>Logout</Nav.Link>
                        </>
                    ) : (
                        <>
                            <Nav.Link href="/login">Login</Nav.Link>
                            <Nav.Link href="/register">Register</Nav.Link>
                        </>
                    )}
                </Nav>
            </Container>
        </Navbar>
    )
}

export default NavbarComponent