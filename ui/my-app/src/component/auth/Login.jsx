import React, {useState} from "react"
import {useNavigate} from 'react-router-dom'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import AuthService from "../../service/AuthService";

const Login = () => {
    let navigate = useNavigate()

    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [message, setMessage] = useState("")

    const onChangeUsername = (e) => {
        const username = e.target.value
        setUsername(username)
    }

    const onChangePassword = (e) => {
        const password = e.target.value
        setPassword(password)
    }

    const handleLogin = (e) => {
        e.preventDefault()
        setMessage("")
        AuthService.login(username, password).then(
            () => {
                navigate("/profile")
                window.location.reload()
            },
            (error) => {
                const resMessage =
                    (error.response &&
                        error.response.data &&
                        error.response.data.message) ||
                    error.message ||
                    error.toString()
                setMessage(resMessage)
            }
        )
    }

    return (
        <div className="col-md-12">
            <div className="card card-container">
                <Form className="container">

                    <Form.Group className="mb-3">
                        <Form.Label>Username</Form.Label>
                        <Form.Control className="username" name="username" type="text"
                                      placeholder="Enter username.."
                                      onChange={onChangeUsername}/>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Password</Form.Label>
                        <Form.Control className="password" name="password" type="password"
                                      placeholder="Enter password.."
                                      onChange={onChangePassword}/>
                    </Form.Group>

                    {message && (
                        <div className="mb-3">
                            <div className="alert alert-danger" role="alert">
                                {message}
                            </div>
                        </div>
                    )}

                    <Button variant="primary" onClick={handleLogin}>
                        Submit
                    </Button>
                </Form>
            </div>
        </div>
    )
}

export default Login
