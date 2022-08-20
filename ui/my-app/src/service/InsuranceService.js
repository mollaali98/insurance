import axios from "axios"
import authHeader from "./AuthHeader";

const API_CREATE_URL = 'http://localhost:8080/insurer/vehicleInsurance'
const API_INSUREES_URL = 'http://localhost:8080/insurer/insuree'
const API_GET_INSURER_URL = 'http://localhost:8080/insurer/vehicleInsurance'
const API_GET_INSUREE_URL = 'http://localhost:8080/insuree/vehicleInsurance'

const createInsurance = (client, insurance) => {
    axios.post(`${API_CREATE_URL}/${client}`, insurance, {headers: authHeader()})
        .then(function (response) {
            console.log(response);
        })
        .catch(function (error) {
            console.log(error);
        })
}

const getInsurerInsurances = () => axios.get(API_GET_INSURER_URL, {headers: authHeader()})
const getInsureeInsurances = () => axios.get(API_GET_INSUREE_URL, {headers: authHeader()})

const getInsurees = () => axios.get(API_INSUREES_URL, {headers: authHeader()})

const InsuranceService = {
    createInsurance,
    getInsurerInsurances,
    getInsureeInsurances,
    getInsurees
}

export default InsuranceService