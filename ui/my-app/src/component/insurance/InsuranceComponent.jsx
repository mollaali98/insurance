import React, {useEffect, useState} from "react"
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import InsuranceService from "../../service/InsuranceService";
import {useNavigate} from "react-router-dom";


const InsuranceComponent = () => {

    const [vehicleInfo, setVehicleInfo] = useState({})
    const [policyNumber, setPolicyNumber] = useState("")
    const [insuredValue, setInsuredValue] = useState("")
    const [duration, setDuration] = useState(0)
    const [premium, setPremium] = useState("")
    const [client, setClient] = useState("")
    const [users, setUsers] = useState([])
    const navigate = useNavigate()

    useEffect(() => {
        InsuranceService.getInsurees().then((response) => {
            setUsers(response.data)
            console.log(response.data)
        })
    }, [])

    const handleChange = (event) => {
        const {name, value} = event.target
        setVehicleInfo({...vehicleInfo, [name]: value})
    }

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(client)
        let inputs = {
            "vehicleInfo": vehicleInfo,
            "policyNumber": policyNumber,
            "insuredValue": insuredValue,
            "duration": duration,
            "premium": premium
        }
        console.log(inputs)
        console.log('You clicked submit.');
        InsuranceService.createInsurance(client, inputs)
        navigate('/insurances')
    }

    return (
        <Form className="container">

            <h1 className="text-center"> Create Insurance </h1>

            <Form.Group className="mb-3" controlId="formBasicSelect">
                <Form.Label>Select Client</Form.Label>
                <Form.Control
                    as="select"
                    value={client}
                    onChange={e => {
                        console.log("e.target.value", e.target.value);
                        setClient(e.target.value);
                    }}
                >
                    {users && (
                        users.map(
                            (user, index) =>
                                <option key={index} value={user.username}>{user.username}</option>
                        ))}
                    {/*<option value="PartyA">PartyA</option>*/}
                </Form.Control>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Registration Number</Form.Label>
                <Form.Control className="registrationNumber" name="registrationNumber" type="text"
                              placeholder="Enter registration number.."
                              onChange={handleChange}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Chasis Number</Form.Label>
                <Form.Control className="chasisNumber" name="chasisNumber" type="text"
                              placeholder="Enter chasis number.."
                              onChange={handleChange}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Make</Form.Label>
                <Form.Control className="make" name="make" type="text"
                              placeholder="Enter make.."
                              onChange={handleChange}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Model</Form.Label>
                <Form.Control className="model" name="model" type="text"
                              placeholder="Enter model.."
                              onChange={handleChange}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Variant</Form.Label>
                <Form.Control className="variant" name="variant" type="text"
                              placeholder="Enter variant.."
                              onChange={handleChange}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Color</Form.Label>
                <Form.Control className="color" name="color" type="text"
                              placeholder="Enter color.."
                              onChange={handleChange}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Fuel Type</Form.Label>
                <Form.Control className="fuelType" name="fuelType" type="text"
                              placeholder="Enter fuel type.."
                              onChange={handleChange}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Policy Number</Form.Label>
                <Form.Control type="text" placeholder="Enter policy number.."
                              onChange={(e) => setPolicyNumber(e.target.value)}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Insured Value</Form.Label>
                <Form.Control type="text" placeholder="Enter insured value.."
                              onChange={(e) => setInsuredValue(e.target.value)}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Duration</Form.Label>
                <Form.Control type="number" placeholder="Enter duration.."
                              onChange={(e) => setDuration(e.target.value)}/>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label>Premium</Form.Label>
                <Form.Control type="text" placeholder="Enter premium.."
                              onChange={(e) => setPremium(e.target.value)}/>
            </Form.Group>

            <Button variant="primary" onClick={handleSubmit}>
                Submit
            </Button>
        </Form>
    )
}

export default InsuranceComponent