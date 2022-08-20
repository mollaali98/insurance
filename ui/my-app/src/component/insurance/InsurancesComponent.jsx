import React, {useEffect, useState} from "react"
import Button from 'react-bootstrap/Button'
import InsuranceService from "../../service/InsuranceService";
import {Link} from "react-router-dom";
import AuthService from "../../service/AuthService";


const InsurancesComponent = () => {

    const [insurances, setInsurances] = useState([])
    const [showClientBoard, setShowClientBoard] = useState(false)


    useEffect(() => {
        getInsurances()
    }, [])

    const getInsurances = () => {
        const user = AuthService.getCurrentUser()
        // ROLE_CLIENT
        // ROLE_INSURANCE
        if (user.roles.includes("ROLE_INSURANCE")) {
            InsuranceService.getRoleInsuranceInsurances().then((response) => {
                setInsurances(response.data)
                console.log(response.data)
            })
        } else if (user.roles.includes("ROLE_CLIENT")) {
            setShowClientBoard(true)
            InsuranceService.getRoleClientInsurances().then((response) => {
                setInsurances(response.data)
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
                    <th> Owner</th>
                    <th> Network Id</th>
                    <th> Policy Number</th>
                    <th> Insured Value</th>
                    <th> Policy Type</th>
                    <th> Start Date</th>
                    <th> End Date</th>
                    <th> Address</th>
                    <th> Property Type</th>
                    <th> Area</th>
                    <th> Location</th>
                </tr>
                </thead>
                <tbody>
                {
                    insurances.map(
                        (insurance, index) =>
                            <tr key={index}>
                                <td> {index + 1}</td>
                                <td> {insurance.owner}</td>
                                <td> {insurance.networkId}</td>
                                <td> {insurance.policyNumber}</td>
                                <td> {insurance.insuredValue}</td>
                                <td> {insurance.policyType}</td>
                                <td> {insurance.startDate}</td>
                                <td> {insurance.endDate}</td>
                                <td> {insurance.propertyInfo.address}</td>
                                <td> {insurance.propertyInfo.propertyType}</td>
                                <td> {insurance.propertyInfo.area}</td>
                                <td> {insurance.propertyInfo.location}</td>
                                {showClientBoard && (
                                    <td>
                                        <Link to={"/insurance/" + insurance.policyNumber + "/claim"}>
                                            <Button size="sm" color="primary">
                                                <p>Add Claim</p>
                                            </Button>
                                        </Link>
                                    </td>
                                )}
                                <td>
                                    <Link to={"/insurance/" + insurance.policyNumber + "/claims"}>
                                        <Button size="sm" color="primary">
                                            <p>Claims</p>
                                        </Button>
                                    </Link>
                                </td>
                            </tr>
                    )
                }
                </tbody>
            </table>
        </div>
    )
}

export default InsurancesComponent