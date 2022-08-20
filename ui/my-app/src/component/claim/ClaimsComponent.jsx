import React, {useEffect, useState} from 'react'
import {useNavigate, useParams} from "react-router-dom"
import ClaimService from "../../service/ClaimService"
import AuthService from "../../service/AuthService";
import Button from "react-bootstrap/Button";

const ClaimsComponent = () => {

    const [claims, setClaims] = useState([])
    const [showInsuranceBoard, setShowInsuranceBoard] = useState(false)
    const {id} = useParams()
    const navigate = useNavigate()

    useEffect(() => {
        getClaims()
    }, [])

    const getClaims = () => {
        const user = AuthService.getCurrentUser()
        // ROLE_CLIENT
        // ROLE_INSURANCE
        if (user.roles.includes("ROLE_INSURANCE")) {
            setShowInsuranceBoard(true)
            ClaimService.getClaimInsurer(id).then((response) => {
                setClaims(response.data)
                console.log(response.data)
            })
        } else if (user.roles.includes("ROLE_CLIENT")) {
            ClaimService.getClaimInsuree(id).then((response) => {
                setClaims(response.data)
                console.log(response.data)
            })
        }
    }

    const handleApprove = (claimNumber) => {
        const claimUpdate = {"claimNumber": claimNumber, "policyNumber": id, "claimStatus": "Approved"}
        console.log("claimUpdate -> " + claimUpdate)
        ClaimService.updateClaim(claimUpdate)
        navigate('/insurances')
    }

    const handleReject = (claimNumber) => {
        const claimUpdate = {"claimNumber": claimNumber, "policyNumber": id, "claimStatus": "Refuse"}
        console.log("claimUpdate -> " + claimUpdate)
        ClaimService.updateClaim(claimUpdate)
        navigate('/insurances')
    }

    return (
        <div className="container">
            <table className="table table-striped">
                <thead>
                <tr>
                    <th> N</th>
                    <th> Number</th>
                    <th> Description</th>
                    <th> Amount</th>
                    <th> Status</th>
                    {showInsuranceBoard && (
                        <>
                            <th></th>
                            <th></th>
                        </>
                    )}
                </tr>
                </thead>
                <tbody>
                {
                    claims.map(
                        (claim, index) =>
                            <tr key={index}>
                                <td> {index + 1}</td>
                                <td> {claim.claimNumber}</td>
                                <td> {claim.claimDescription}</td>
                                <td> {claim.claimAmount}</td>
                                <td> {claim.claimStatus}</td>
                                {(showInsuranceBoard && claim.claimStatus === "Waiting") && (
                                    <>
                                        <td>
                                            <Button size="sm" color="primary"
                                                    onClick={() => handleApprove(claim.claimNumber)}>
                                                <p>Approve</p>
                                            </Button>
                                        </td>
                                        <td>
                                            <Button size="sm" color="primary"
                                                    onClick={() => handleReject(claim.claimNumber)}>
                                                <p>Refuse</p>
                                            </Button>
                                        </td>
                                    </>
                                )}
                            </tr>
                    )
                }
                </tbody>
            </table>
        </div>
    )
}

export default ClaimsComponent