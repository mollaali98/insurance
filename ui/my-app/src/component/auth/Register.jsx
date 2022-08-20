import React, {useState, useRef} from "react"
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import AuthService from "../../service/AuthService";


const Register = () => {
    const [username, setUsername] = useState("")
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [role, setRole] = useState("insurer")
    const [successful, setSuccessful] = useState(false)
    const [message, setMessage] = useState("")

    const onChangeUsername = (e) => {
        const username = e.target.value
        setUsername(username)
    }
    const onChangeEmail = (e) => {
        const email = e.target.value
        setEmail(email)
    }
    const onChangePassword = (e) => {
        const password = e.target.value
        setPassword(password)
    }

    const handleRegister = (e) => {
        e.preventDefault()
        setMessage("")
        setSuccessful(false)
        AuthService.register(username, email, password, [role]).then(
            (response) => {
                setMessage(response.data.message)
                setSuccessful(true)
            },
            (error) => {
                const resMessage =
                    (error.response &&
                        error.response.data &&
                        error.response.data.message) ||
                    error.message ||
                    error.toString()
                setMessage(resMessage)
                setSuccessful(false)
            }
        )
    }

    return (
        <div className="col-md-12">
            <div className="card card-container">
                <Form>
                    {!successful && (
                        <div>
                            <Form.Group className="mb-3">
                                <Form.Label>Username</Form.Label>
                                <Form.Control className="username" name="username" type="text"
                                              placeholder="Enter username.."
                                              onChange={onChangeUsername}/>
                            </Form.Group>

                            <Form.Group className="mb-3">
                                <Form.Label>Email</Form.Label>
                                <Form.Control className="email" name="email" type="email"
                                              placeholder="Enter email.."
                                              onChange={onChangeEmail}/>
                            </Form.Group>

                            <Form.Group className="mb-3">
                                <Form.Label>Password</Form.Label>
                                <Form.Control className="password" name="password" type="password"
                                              placeholder="Enter password.."
                                              onChange={onChangePassword}/>
                            </Form.Group>

                            <Form.Group className="mb-3" controlId="formBasicSelect">
                                <Form.Label>Select Role</Form.Label>
                                <Form.Control
                                    as="select"
                                    value={role}
                                    onChange={e => {
                                        console.log("e.target.value", e.target.value);
                                        setRole(e.target.value);
                                    }}
                                >
                                    <option value="insuree">Insuree</option>
                                    <option value="insurer">Insurer</option>
                                </Form.Control>
                            </Form.Group>

                            <Button variant="primary" onClick={handleRegister}>
                                Submit
                            </Button>
                        </div>
                    )}
                    {message && (
                        <div className="form-group">
                            <div
                                className={successful ? "alert alert-success" : "alert alert-danger"}
                                role="alert"
                            >
                                {message}
                            </div>
                        </div>
                    )}
                </Form>
            </div>
        </div>
    )
}

export default Register
