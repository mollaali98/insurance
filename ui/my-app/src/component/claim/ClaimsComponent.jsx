import React, {useEffect, useState} from 'react'
import {useParams} from "react-router-dom"
import ClaimService from "../../service/ClaimService"
import AuthService from "../../service/AuthService";

const ClaimsComponent = () => {

    const [claims, setClaims] = useState([])
    const {id} = useParams()

    useEffect(() => {
        getClaims()
    }, [])

    const getClaims = () => {
        const user = AuthService.getCurrentUser()
        // ROLE_CLIENT
        // ROLE_INSURANCE
        if (user.roles.includes("ROLE_INSURANCE")) {
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

    return (
        <div className="container">
            <table className="table table-striped">
                <thead>
                <tr>
                    <th> N</th>
                    <th> Number</th>
                    <th> Description</th>
                    <th> Amount</th>
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
                            </tr>
                    )
                }
                </tbody>
            </table>
        </div>
    )
}

export default ClaimsComponent