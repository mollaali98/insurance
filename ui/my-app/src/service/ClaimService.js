import axios from "axios";
import authHeader from "./AuthHeader";


const API_BASE_URL = 'http://localhost:8080/client/insurance/claim'
const API_GET_INSURER_URL = 'http://localhost:8080/insurance/claim'
const API_GET_INSUREE_URL = 'http://localhost:8080/client/insurance/claim'


const createClaim = (policyNumber, claim) => {
    axios.post(`${API_BASE_URL}/${policyNumber}`, claim, {headers: authHeader()})
        .then(function (response) {
            console.log(response);
        })
        .catch(function (error) {
            console.log(error);
        })
}

const getClaimInsurer = (policyNumber) => axios.get(`${API_GET_INSURER_URL}/${policyNumber}`, {headers: authHeader()})
const getClaimInsuree = (policyNumber) => axios.get(`${API_GET_INSUREE_URL}/${policyNumber}`, {headers: authHeader()})

const ClaimService = {
    createClaim,
    getClaimInsurer,
    getClaimInsuree
}

export default ClaimService