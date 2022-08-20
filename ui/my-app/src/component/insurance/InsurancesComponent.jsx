import React, {useEffect, useState} from "react"
import Button from 'react-bootstrap/Button'
import InsuranceService from "../../service/InsuranceService";
import {Link} from "react-router-dom";
import AuthService from "../../service/AuthService";


const InsurancesComponent = () => {

    const [insurances, setInsurances] = useState([])
    const [showInsurerBoard, setShowInsurerBoard] = useState(false)


    useEffect(() => {
        getInsurances()
    }, [])

    const getInsurances = () => {
        const user = AuthService.getCurrentUser()
        if (user.roles.includes("ROLE_INSURER")) {
            setShowInsurerBoard(true)
            InsuranceService.getInsurerInsurances().then((response) => {
                setInsurances(response.data)
                console.log(response.data)
            })
        } else if (user.roles.includes("ROLE_INSUREE")) {
            InsuranceService.getInsureeInsurances().then((response) => {
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
                    <th> Policy Number</th>
                    <th> Insured Value</th>
                    <th> Duration</th>
                    <th> Premium</th>
                    <th> Registration Number</th>
                    <th> Chasis Number</th>
                    <th> Make</th>
                    <th> Model</th>
                    <th> Variant</th>
                    <th> Color</th>
                    <th> Fuel Type</th>
                </tr>
                </thead>
                <tbody>
                {
                    insurances.map(
                        (insurance, index) =>
                            <tr key={index}>
                                <td> {index + 1}</td>
                                <td> {insurance.policyNumber}</td>
                                <td> {insurance.insuredValue}</td>
                                <td> {insurance.duration}</td>
                                <td> {insurance.premium}</td>
                                <td> {insurance.vehicleInfo.registrationNumber}</td>
                                <td> {insurance.vehicleInfo.chasisNumber}</td>
                                <td> {insurance.vehicleInfo.make}</td>
                                <td> {insurance.vehicleInfo.model}</td>
                                <td> {insurance.vehicleInfo.variant}</td>
                                <td> {insurance.vehicleInfo.color}</td>
                                <td> {insurance.vehicleInfo.fuelType}</td>
                                {showInsurerBoard && (
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