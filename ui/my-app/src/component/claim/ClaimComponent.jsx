import React, {useEffect, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {Button, Container, Form, FormGroup} from 'react-bootstrap';
import ClaimService from "../../service/ClaimService";


const ClaimComponent = () => {
    const initialFormState = {
        claimNumber: '',
        claimDescription: '',
        claimAmount: 0
    }
    const [claim, setClaim] = useState(initialFormState);
    const navigate = useNavigate()
    const {id} = useParams()

    const handleChange = (event) => {
        const {name, value} = event.target
        setClaim({...claim, [name]: value})
    }

    const handleSubmit = (event) => {
        event.preventDefault()
        ClaimService.createClaim(id, claim)
        setClaim(initialFormState)
        navigate('/insurances')
    }

    return (
        <div>
            <Container>
                <Form onSubmit={handleSubmit}>
                    <FormGroup className="mb-3">
                        <Form.Label>Claim Number</Form.Label>
                        <Form.Control type="text" name="claimNumber" id="claimNumber" value={claim.claimNumber || ''}
                                      onChange={handleChange}/>
                    </FormGroup>
                    <FormGroup className="mb-3">
                        <Form.Label>Claim Description</Form.Label>
                        <Form.Control type="text" name="claimDescription" id="claimDescription"
                                      value={claim.claimDescription || ''}
                                      onChange={handleChange}/>
                    </FormGroup>
                    <FormGroup className="mb-3">
                        <Form.Label>Claim Amount</Form.Label>
                        <Form.Control type="number" name="claimAmount" id="claimAmount" value={claim.claimAmount || ''}
                                      onChange={handleChange}/>
                    </FormGroup>
                    <FormGroup className="mb-3">
                        <Button color="primary" type="submit">Save</Button>
                    </FormGroup>
                </Form>
            </Container>
        </div>
    )
}

export default ClaimComponent