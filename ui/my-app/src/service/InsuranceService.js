import axios from "axios"
import authHeader from "./AuthHeader";

const API_CREATE_URL = 'http://localhost:8080/client/insurance'
const API_INSUREES_URL = 'http://localhost:8080/client/insurances'
const API_GET_INSURER_URL = 'http://localhost:8080/insurance'
const API_GET_INSUREE_URL = 'http://localhost:8080/client/insurance'

const createInsurance = (insurance) => {
    axios.post(API_CREATE_URL, insurance, {headers: authHeader()})
        .then(function (response) {
            console.log(response);
        })
        .catch(function (error) {
            console.log(error);
        })
}

const getRoleInsuranceInsurances = () => axios.get(API_GET_INSURER_URL, {headers: authHeader()})
const getRoleClientInsurances = () => axios.get(API_GET_INSUREE_URL, {headers: authHeader()})

const getInsurees = () => axios.get(API_INSUREES_URL, {headers: authHeader()})

const InsuranceService = {
    createInsurance,
    getRoleInsuranceInsurances,
    getRoleClientInsurances,
    getInsurees
}

export default InsuranceService