import React, {useEffect, useState} from "react"
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import InsuranceService from "../../service/InsuranceService";
import {useNavigate} from "react-router-dom";
import Container from "react-bootstrap/Container";


const InsuranceComponent = () => {

    const [policyNumber, setPolicyNumber] = useState("")
    const policyTypes = ["Silver", "Gold", "Platinum"]
    const [policyType, setPolicyTypes] = useState(policyTypes[0])
    const [startDate, setStartDate] = useState()
    const [insuredValue, setInsuredValue] = useState(0.00)
    const [address, setAddress] = useState("")
    const propertyTypes = ["Apartment", "House", "Villa"]
    const [propertyType, setPropertyType] = useState(propertyTypes[0])
    const [area, setArea] = useState(0.00)
    const [location, setLocation] = useState("")
    const navigate = useNavigate()

    useEffect(() => {
    }, [])

    const handleSubmit = (e) => {
        e.preventDefault();
        let inputs = {
            "policyNumber": policyNumber,
            "insuredValue": insuredValue,
            "policyType": policyType,
            "startDate": startDate,
            "propertyInfo": {
                "address": address,
                "propertyType": propertyType,
                "area": area,
                "location": location
            }
        }
        console.log(inputs)
        console.log('You clicked submit.');
        InsuranceService.createInsurance(inputs)
        navigate('/insurances')
    }

    return (
        <Container className="p-3">
            <Container className="p-5 mb-4 bg-light rounded-3">
                <Form className="container">
                    <h1 className="text-center"> Create Insurance </h1>
                    <Form.Group className="mb-3">
                        <Form.Label>Policy Number</Form.Label>
                        <Form.Control type="text"
                                      className="col-md-4"
                                      placeholder="Enter policy number.."
                                      onChange={(e) => setPolicyNumber(e.target.value)}/>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Insured Value</Form.Label>
                        <Form.Control type="number" placeholder="Enter insured value.." step="0.01"
                                      onChange={(e) => setInsuredValue(e.target.value)}/>
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="formBasicSelect">
                        <Form.Label>Policy Type</Form.Label>
                        <Form.Control
                            as="select"
                            value={policyType}
                            onChange={e => {
                                console.log("e.target.value", e.target.value);
                                setPolicyTypes(e.target.value);
                            }}
                        >
                            {policyTypes && (
                                policyTypes.map(
                                    (type, index) =>
                                        <option key={index} value={type}>{type}</option>
                                ))}
                        </Form.Control>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Start Date</Form.Label>
                        <Form.Control type="date"
                                      placeholder="Enter Start date.."
                                      onChange={(e) => {
                                          console.log("e.target.value", e.target.value)
                                          setStartDate(e.target.value)
                                      }}/>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Address</Form.Label>
                        <Form.Control type="text" placeholder="Enter address.."
                                      onChange={(e) => setAddress(e.target.value)}/>
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="formBasicSelect">
                        <Form.Label>Property Type</Form.Label>
                        <Form.Control
                            as="select"
                            value={propertyType}
                            onChange={e => {
                                console.log("e.target.value", e.target.value);
                                setPropertyType(e.target.value);
                            }}
                        >
                            {propertyTypes && (
                                propertyTypes.map(
                                    (type, index) =>
                                        <option key={index} value={type}>{type}</option>
                                ))}
                        </Form.Control>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Area</Form.Label>
                        <Form.Control type="number" placeholder="Enter area.." step="0.01"
                                      onChange={(e) => setArea(e.target.value)}/>
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Location</Form.Label>
                        <Form.Control type="text" placeholder="Enter location.."
                                      onChange={(e) => setLocation(e.target.value)}/>
                    </Form.Group>
                    <Button variant="primary" onClick={handleSubmit}>
                        Submit
                    </Button>
                </Form>
            </Container>
        </Container>
    )
}

export default InsuranceComponent